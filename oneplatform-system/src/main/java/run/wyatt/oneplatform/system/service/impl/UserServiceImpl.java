package run.wyatt.oneplatform.system.service.impl;

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
import run.wyatt.oneplatform.system.service.UserService;

import java.util.ArrayList;
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

    @Override
    public boolean invalidUsernameFormat(String username) {
        return !username.matches(USERNAME_REGEXP);
    }

    @Override
    public boolean invalidPasswordFormat(String password) {
        return !password.matches(PASSWORD_REGEXP);
    }

    @Override
    public Long createUser(String username, String password) {
        log.info("输入参数: username={} password=*", username);

        // 判断用户是否已被注册
        User user = null;
        try {
            user = userDao.findByUsername(username);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
        if (user != null) {
            throw new BusinessException("用户已注册");
        }

        // 密码加密
        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encode(password, salt);
        log.info("密码加密完成");

        // 保存到数据库
        User record = new User();
        record.setUsername(username);
        record.setPassword(encryptedPassword);
        record.setSalt(salt);
        Long userId = null;
        try {
            userId = userDao.insert(record);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
        log.info("用户成功注册到数据库: userId={}", userId);

        // 为用户绑定默认角色
        if (userId != null) {
            bindRole(userId, SysConst.DEFAULT_ROLE_ID);
        }

        log.info("成功创建用户");
        return userId;
    }

    @Override
    public boolean bindRole(Long userId, Long roleId) {
        log.info("输入参数: userId={}, roleId={}", userId, roleId);

        try {
            long affected = userRoleDao.insert(userId, roleId);
            log.info("成功为用户绑定角色");
            return affected == 1;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
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

    @Override
    public List<String> getRoleIdentifiersOfUser(Long userId) {
        log.info("输入参数: userId={}", userId);

        List<String> roles = null;
        Integer roleChanged = (Integer) redis.opsForValue().get(SysConst.ROLE_CHANGED);
        if (roleChanged == null || roleChanged == 0) {
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
        }
        log.info("用户的角色标识列表: {}", roles);
        return roles;
    }

    @Override
    public List<String> getAuthIdentifiersOfUser(Long userId) {
        log.info("输入参数: userId={}", userId);

        List<String> auths = null;
        Integer authChanged = (Integer) redis.opsForValue().get(SysConst.AUTH_CHANGED);
        if (authChanged == null || authChanged == 0) {
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
        }
        log.info("用户的权限标识列表: {}", auths);
        return auths;
    }
}
