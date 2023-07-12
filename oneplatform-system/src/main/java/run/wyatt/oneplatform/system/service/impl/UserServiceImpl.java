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
        roleService.setRefreshRoleRedisTrue(userId);
        authService.setRefreshAuthRedisTrue(userId);
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
            // TODO
            roleService.updateRoleDbChangeTime();
            authService.updateAuthDbChangeTime();
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
            // TODO
            roleService.updateRoleDbChangeTime();
            authService.updateAuthDbChangeTime();
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

    private void updateRedisProfile(User profile) {
        StpUtil.getSession().set(CommonConst.REDIS_PROFILE_KEY, profile);
        log.info("已更新缓存的profile信息: {}", profile);
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

    @Override
    public List<User> listAllUsersNoSensitives() {
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
    }
}
