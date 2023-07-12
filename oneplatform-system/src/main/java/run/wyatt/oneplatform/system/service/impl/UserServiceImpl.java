package run.wyatt.oneplatform.system.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.cosnt.CommonConst;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.common.util.PasswordUtil;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.dao.RoleDao;
import run.wyatt.oneplatform.system.dao.UserDao;
import run.wyatt.oneplatform.system.dao.UserRoleDao;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.entity.Role;
import run.wyatt.oneplatform.system.model.entity.User;
import run.wyatt.oneplatform.system.service.AuthService;
import run.wyatt.oneplatform.system.service.RoleService;
import run.wyatt.oneplatform.system.service.UserService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/9 10:38
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private static final String USERNAME_REGEXP = "^[A-Za-z0-9]{1,16}$";
    private static final String PASSWORD_REGEXP = "^[A-Za-z0-9.~!@#$%^&*_?]{6,16}$";
    @Autowired
    private RedisTemplate<String, Object> redis;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private AuthDao authDao;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthService authService;

    @Override
    public boolean wrongUsernameFormat(String username) {
        return !username.matches(USERNAME_REGEXP);
    }

    @Override
    public boolean wrongPasswordFormat(String password) {
        return !password.matches(PASSWORD_REGEXP);
    }

    @Override
    public User createUser(String username, String password) {
        log.info("输入参数: username={} password=*", username);

        // 判断用户是否已被注册
        if (userDao.findByUsername(username) != null) {
            throw new BusinessException("用户已注册");
        }

        // 密码加密
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encode(password, salt);
        log.info("密码加密完成");

        // 保存到数据库
        User record = new User();
        record.setId(null);
        record.setUsername(username);
        record.setPassword(encryptedPassword);
        record.setSalt(salt);

        if (userDao.insert(record) == 1) {
            log.info("用户成功注册到数据库: userId={}", record.getId());

            // 为用户绑定默认角色
            try {
                bind(record.getId(), SysConst.DEFAULT_ROLE_ID);
                log.info("成功为新注册用户绑定默认角色");
            } catch (Exception e) {
                throw new BusinessException("用户绑定默认角色失败");
            }

            log.info("创建用户成功完成");
            return record;
        } else {
            throw new BusinessException("用户创建失败");
        }
    }

    @Override
    public void bind(Long userId, Long roleId) {
        log.info("输入参数: userId={}, roleId={}", userId, roleId);
        if (userId == null || roleId == null) {
            throw new BusinessException("参数错误");
        }

        if (userRoleDao.insert(userId, roleId) == 1) {
            log.info("绑定成功");
        } else {
            throw new BusinessException("绑定失败");
        }

        log.info("为用户标记须更新角色和权限标识符缓存");
        setRefreshRoleRedis(userId);
        setRefreshAuthRedis(userId);
    }

    @Override
    public List<Long> bind(Long userId, List<Long> roleIds) {
        log.info("输入参数: userId={}, roleIds={}", userId, roleIds);
        if (userId == null || roleIds == null) throw new BusinessException("参数错误");

        List<Long> failList = new ArrayList<>();
        for (Long roleId : roleIds) {
            try {
                long rows = userRoleDao.insert(userId, roleId);
                log.info("绑定成功: (userId={}, roleId={})", userId, roleId);
            } catch (Exception e) {
                failList.add(roleId);
            }
        }
        log.info("绑定失败的roleId：{}", failList);

        // 有授权成功时，要更新标志，以动态更新用户角色、权限缓存
        if (failList.size() < roleIds.size()) {
            roleService.updateRoleDbChanged();
            authService.updateAuthDbChanged();
        }

        return failList;
    }

    @Override
    public List<Long> unbind(Long userId, List<Long> roleIds) {
        log.info("输入参数: userId={}, roleIds={}", userId, roleIds);
        if (userId == null || roleIds == null) throw new BusinessException("参数错误");

        List<Long> failList = new ArrayList<>();
        for (Long roleId : roleIds) {
            try {
                long rows = userRoleDao.delete(userId, roleId);
                log.info("解除绑定成功: (userId={}, roleId={})", userId, roleId);
            } catch (Exception e) {
                failList.add(roleId);
            }
        }
        log.info("解除绑定失败的roleId：{}", failList);

        // 有授权成功时，要更新标志，以动态更新用户角色、权限缓存
        if (failList.size() < roleIds.size()) {
            roleService.updateRoleDbChanged();
            authService.updateAuthDbChanged();
        }

        return failList;
    }

    @Override
    public User editProfile(Long userId, User profile) {
        log.info("输入参数: userId={}, profile={}", userId, profile);

        String nickname = profile.getNickname();

        if (nickname != null && !nickname.isEmpty()) {
            if (userDao.findByNickname(profile.getNickname()) != null) {
                throw new BusinessException("昵称已被占用");
            }
        }

        if (userDao.update(userId, profile) == 1) {
            log.info("成功修改用户信息");

            profile = userDao.findById(userId);
            profile.setPassword(null);
            profile.setSalt(null);
            updateRedisProfile(profile);

            return profile;
        } else {
            throw new BusinessException("修改用户信息失败");
        }
    }

    @Override
    public User verifyByUsername(String username, String password) {
        log.info("输入参数: username={}, password=*", username);


        User user = userDao.findByUsername(username);
        if (user == null || !user.getPassword().equals(PasswordUtil.encode(password, user.getSalt()))) {
            throw new BusinessException("用户名或密码错误");
        }
        log.info("用户名和密码通过验证");

        return user;
    }

    private void updateRedisProfile(User profile) {
        StpUtil.getSession().set(CommonConst.REDIS_PROFILE_KEY, profile);
        log.info("已更新缓存的profile信息: {}", profile);
    }

    /**
     * 若用户已登录，将Session的refreshRoleRedis设置为1
     * 表示用户查询其缓存的权限标识时应该访问数据库来更新缓存
     *
     * @param userId 用户ID
     */
    private void setRefreshRoleRedis(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            userSession.set(SysConst.REFRESH_ROLE_REDIS, 1);
            log.info("已设置refreshRoleRedis");
            return;
        }
        log.info("用户 {} 未登录，无需设置已设置refreshRoleRedis", userId);
    }

    /**
     * 若用户已登录，将Session的refreshAuthRedis设置为1
     * 表示用户查询其缓存的权限标识时应该访问数据库来更新缓存
     *
     * @param userId 用户ID
     */
    private void setRefreshAuthRedis(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            userSession.set(SysConst.REFRESH_AUTH_REDIS, 1);
            log.info("已设置refreshAuthRedis");
            return;
        }
        log.info("用户 {} 未登录，无需设置已设置refreshAuthRedis", userId);
    }

    private void setRefreshRoleRedisZero(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            userSession.set(SysConst.REFRESH_ROLE_REDIS, 0);
            log.info("已设置refreshRoleRedis=0");
        }
    }

    private void setRefreshAuthRedisZero(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            userSession.set(SysConst.REFRESH_AUTH_REDIS, 0);
            log.info("已设置refreshAuthRedis=0");
        }
    }


    /**
     * 判断不需要更新缓存的role标识
     *
     * @param userId 用户ID
     * @return true: 不需要更新 / false: 需要更新
     */
    private boolean noNeedRefreshRoleRedis(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            return 0 == (int) userSession.get(SysConst.REFRESH_ROLE_REDIS);
        }
        return true;
    }

    /**
     * 判断不需要更新缓存的auth标识
     *
     * @param userId 用户ID
     * @return true: 不需要更新 / false: 需要更新
     */
    private boolean noNeedRefreshAuthRedis(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            return 0 == (int) userSession.get(SysConst.REFRESH_AUTH_REDIS);
        }
        return true;
    }

    private void updateRoleRedisChangeTime() {
        Date now = new Date();
        StpUtil.getSession().set(SysConst.ROLE_REDIS_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新roleRedisChangeTime缓存为: {}", sdf.format(now));
    }

    private void updateAuthRedisChangeTime() {
        Date now = new Date();
        StpUtil.getSession().set(SysConst.AUTH_REDIS_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新authRedisChangeTime缓存为: {}", sdf.format(now));
    }

    // 先查询缓存的情况：数据库无变更（对比时间标记） and 绑定关系无变更（refreshRoleRedis/refreshAuthRedis是否值为1）
    // 一、判断数据库无变更
    //      1、无dbChanged ==> 数据库无变更
    //      2、有dbChanged、有redisChanged，且dbChanged <= redisChanged ==> 数据库有变更，但是已经更新过缓存了
    // 二、判断绑定关系无变更
    //      缓存中refreshRoleRedis/refreshAuthRedis不存在，或者值为0
    @Override
    public List<String> getRoleIdentifiers(Long userId) {
        log.info("输入参数: userId={}", userId);

        Date t1 = (Date) redis.opsForValue().get(SysConst.ROLE_DB_CHANGE_TIME);
        Date t2 = (Date) StpUtil.getSession().get(SysConst.ROLE_REDIS_CHANGE_TIME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("数据库更新时间： {}", t1 == null ? null : sdf.format(t1));
        log.info("缓存更新时间: {}", t2 == null ? null : sdf.format(t2));

        List<String> roles = null;
        if ((t1 == null || (t2 != null && t1.compareTo(t2) <= 0)) && noNeedRefreshRoleRedis(userId)) {
            log.info("先查询Redis的Session缓存");
            roles = (List<String>) StpUtil.getSession().get(CommonConst.REDIS_ROLES_KEY);
        }

        if (roles == null) {
            log.info("无缓存或缓存不是最新，查询数据库，并更新缓存");
            List<Role> activatedRoles = roleDao.findActivatedByUserId(userId);
            roles = new ArrayList<>();
            for (Role item : activatedRoles) {
                roles.add(item.getIdentifier());
            }
            log.info("完成标识符字符串列表提取");
            StpUtil.getSession().set(CommonConst.REDIS_ROLES_KEY, roles);
            log.info("已更新缓存");
            updateRoleRedisChangeTime();
            setRefreshRoleRedisZero(userId);
        }

        log.info("用户的角色标识列表: {}", roles);
        return roles;
    }

    // 规则参考getRoleIdentifiers()前写的规则
    @Override
    public List<String> getAuthIdentifiers(Long userId) {
        log.info("输入参数: userId={}", userId);

        Date t1 = (Date) redis.opsForValue().get(SysConst.AUTH_DB_CHANGE_TIME);
        Date t2 = (Date) StpUtil.getSession().get(SysConst.AUTH_REDIS_CHANGE_TIME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("数据库更新时间： {}", t1 == null ? null : sdf.format(t1));
        log.info("缓存更新时间: {}", t2 == null ? null : sdf.format(t2));

        List<String> auths = null;
        if (t1 == null || (t2 != null && t1.compareTo(t2) <= 0)) {
            log.info("先查询Redis的Session缓存");
            auths = (List<String>) StpUtil.getSession().get(CommonConst.REDIS_AUTHS_KEY);
        }

        if (auths == null) {
            log.info("无缓存或缓存不是最新，查询数据库，并更新缓存");
            List<Auth> activatedAuths = authDao.findActivatedByUserId(userId);
            auths = new ArrayList<>();
            for (Auth item : activatedAuths) {
                auths.add(item.getIdentifier());
            }
            log.info("完成标识符字符串列表提取");
            StpUtil.getSession().set(CommonConst.REDIS_AUTHS_KEY, auths);
            log.info("已更新缓存");
            updateAuthRedisChangeTime();
            setRefreshAuthRedisZero(userId);
        }

        log.info("用户的权限标识列表: {}", auths);
        return auths;
    }

    @Override
    public List<User> listAllUsersDesensitized() {
        try {
            List<User> userList = userDao.findAll();
            log.info("成功查询全部用户");

            for (int i = 0; i < userList.size(); i++) {
                User user = userList.get(i);
                user.setPassword(null);
                user.setSalt(null);
                userList.set(i, user);
            }
            log.info("成功脱敏: {}", userList);

            return userList;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
    }
}
