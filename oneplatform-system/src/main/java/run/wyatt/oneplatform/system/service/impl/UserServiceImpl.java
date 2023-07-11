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

        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            log.info("用户 {} 已登录，绑定新角色后，为其标记须更新角色和权限标识符缓存", userId);
            // TODO
        }
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
            User user;
            try {
                user = userDao.findByNickname(profile.getNickname());
            } catch (Exception e) {
                log.info(e.getMessage());
                throw new DatabaseException();
            }
            if (user != null) {
                throw new BusinessException("昵称已被占用");
            }
        }

        long rows;
        try {
            rows = userDao.update(userId, profile);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        if (rows == 1) {
            log.info("成功修改用户信息");

            User user = null;
            try {
                user = userDao.findById(userId);
            } catch (Exception e) {
                log.info(e.getMessage());
                throw new DatabaseException();
            }
            updateProfileRedis(user);

            return profile;
        } else {
            throw new BusinessException("修改用户信息失败");
        }
    }

    @Override
    public User verifyByUsername(String username, String password) {
        log.info("输入参数: username={}, password=*", username);

        // 查询数据库
        User user = null;
        try {
            user = userDao.findByUsername(username);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        // 判断用户名和密码是否正确
        if (user == null || !user.getPassword().equals(PasswordUtil.encode(password, user.getSalt()))) {
            throw new BusinessException("用户名或密码错误");
        }
        log.info("用户名和密码通过验证");

        return user;
    }

    private void updateProfileRedis(User profile) {
        StpUtil.getSession().set(CommonConst.REDIS_PROFILE_KEY, profile);
        log.info("已更新缓存的profile信息: {}", profile);
    }

    private void updateRoleRedisChanged() {
        Date now = new Date();
        StpUtil.getSession().set(SysConst.ROLE_REDIS_CHANGED, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新roleRedisChanged缓存为: {}", sdf.format(now));
    }

    private void updateAuthRedisChanged() {
        Date now = new Date();
        StpUtil.getSession().set(SysConst.AUTH_REDIS_CHANGED, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新authRedisChanged缓存为: {}", sdf.format(now));
    }

    @Override
    public List<String> getRoleIdentifiersOfUser(Long userId) {
        log.info("输入参数: userId={}", userId);

        List<String> roles = null;

        Date dbChanged = (Date) redis.opsForValue().get(SysConst.ROLE_DB_CHANGED);
        Date redisChanged = (Date) StpUtil.getSession().get(SysConst.ROLE_REDIS_CHANGED);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("dbChanged={}, redisChanged={}", dbChanged == null ? null : sdf.format(dbChanged), redisChanged == null ? null : sdf.format(redisChanged));

        // 先查询缓存的情况：
        // 1、无dbChanged：即从来没有更新过数据库，此时先查询缓存，缓存没数据才查询数据库
        // 2、有dbChanged，但是无redisChanged：即数据库更新了但缓存没更新，此时应查询数据库
        // 3、有dbChanged、有redisChanged：这时候比较两者前后：
        //      --> 如果dbChanged <= redisChanged：表示数据库更新后，已经更新过缓存了，此时先查缓存
        //      --> 如果dbChanged > redisChanged：表示缓存还没更新，此时应查询数据库
        if (dbChanged == null || (redisChanged != null && dbChanged.compareTo(redisChanged) <= 0)) {
            log.info("数据库角色表未发生变更，先查询Redis的Session缓存");
            roles = (List<String>) StpUtil.getSession().get(CommonConst.REDIS_ROLES_KEY);
        }

        if (roles == null) {
            log.info("数据库角色表曾经发生变更，或Redis的Session缓存无角色数据，查询数据库");
            try {
                List<Role> roleList = roleDao.findValidByUserId(userId);
                roles = new ArrayList<>();
                for (Role item : roleList) {
                    if (item.getActivated()) {
                        roles.add(item.getIdentifier());
                    }
                }
                log.info("完成标识符字符串列表提取");
            } catch (Exception e) {
                log.info(e.getMessage());
                throw new DatabaseException();
            }
            StpUtil.getSession().set(CommonConst.REDIS_ROLES_KEY, roles);
            log.info("已更新缓存");
            updateRoleRedisChanged();
        }

        log.info("用户的角色标识列表: {}", roles);
        return roles;
    }

    @Override
    public List<String> getAuthIdentifiersOfUser(Long userId) {
        log.info("输入参数: userId={}", userId);

        List<String> auths = null;

        Date dbChanged = (Date) redis.opsForValue().get(SysConst.AUTH_DB_CHANGED);
        Date redisChanged = (Date) StpUtil.getSession().get(SysConst.AUTH_REDIS_CHANGED);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("dbChanged={}, redisChanged={}", dbChanged == null ? null : sdf.format(dbChanged), redisChanged == null ? null : sdf.format(redisChanged));

        // 先查询缓存的情况：
        // 1、无dbChanged：即从来没有更新过数据库，此时先查询缓存，缓存没数据才查询数据库
        // 2、有dbChanged，但是无redisChanged：即数据库更新了但缓存没更新，此时应查询数据库
        // 3、有dbChanged、有redisChanged：这时候比较两者前后：
        //      --> 如果dbChanged <= redisChanged：表示数据库更新后，已经更新过缓存了，此时先查缓存
        //      --> 如果dbChanged > redisChanged：表示缓存还没更新，此时应查询数据库
        if (dbChanged == null || (redisChanged != null && dbChanged.compareTo(redisChanged) <= 0)) {
            log.info("数据库权限表未发生变更，先查询Redis的Session缓存");
            auths = (List<String>) StpUtil.getSession().get(CommonConst.REDIS_AUTHS_KEY);
        }

        if (auths == null) {
            log.info("数据库权限表曾经发生变更，或Redis的Session缓存无权限数据，查询数据库");
            try {
                List<Auth> authList = authDao.findValidByUserId(userId);
                auths = new ArrayList<>();
                for (Auth item : authList) {
                    if (item.getActivated()) {
                        auths.add(item.getIdentifier());
                    }
                }
                log.info("完成标识符字符串列表提取");
            } catch (Exception e) {
                log.info(e.getMessage());
                throw new DatabaseException();
            }
            StpUtil.getSession().set(CommonConst.REDIS_AUTHS_KEY, auths);
            log.info("已更新缓存");
            updateAuthRedisChanged();
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
