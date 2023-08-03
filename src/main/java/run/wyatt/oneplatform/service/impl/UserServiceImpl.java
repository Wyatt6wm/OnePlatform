package run.wyatt.oneplatform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.dao.AuthDao;
import run.wyatt.oneplatform.dao.RoleDao;
import run.wyatt.oneplatform.dao.UserDao;
import run.wyatt.oneplatform.dao.UserRoleDao;
import run.wyatt.oneplatform.model.constant.RedisConst;
import run.wyatt.oneplatform.model.constant.RoleConst;
import run.wyatt.oneplatform.model.entity.User;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.service.AuthService;
import run.wyatt.oneplatform.service.RoleService;
import run.wyatt.oneplatform.service.UserService;
import run.wyatt.oneplatform.util.PasswordUtil;

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
                bind(record.getId(), RoleConst.DEFAULT_ID);
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
        if (roleId.equals(RoleConst.SUPER_ADMIN_ID)) {
            throw new BusinessException("无法为指定用户绑定超级管理员");
        }
        if (roleId.equals(RoleConst.ADMIN_ID) && !StpUtil.getRoleList().contains(RoleConst.SUPER_ADMIN_IDENTIFIER)) {
            throw new BusinessException("权限不足，无法为指定用户绑定管理员");
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
                if (roleId.equals(RoleConst.SUPER_ADMIN_ID)) {
                    throw new BusinessException("无法为指定用户绑定超级管理员");
                }
                if (roleId.equals(RoleConst.ADMIN_ID) && !StpUtil.getRoleList().contains(RoleConst.SUPER_ADMIN_IDENTIFIER)) {
                    throw new BusinessException("权限不足，无法为指定用户绑定管理员");
                }

                userRoleDao.insert(userId, roleId);
                log.info("bind: (userId={}, roleId={})", userId, roleId);
            } catch (Exception e) {
                log.info(e.getMessage());
                failList.add(roleId);
            }
        }
        log.info("绑定失败的roleId：{}", failList);

        if (failList.size() < roleIds.size()) {
            log.info("有绑定成功，为用户标记须更新角色和权限标识符缓存");
            roleService.setRefreshRoleRedisTrue(userId);
            authService.setRefreshAuthRedisTrue(userId);
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
                if (roleId.equals(RoleConst.SUPER_ADMIN_ID)) {
                    throw new BusinessException("无法为指定用户解除超级管理员的绑定");
                }
                if (roleId.equals(RoleConst.ADMIN_ID) && !StpUtil.getRoleList().contains(RoleConst.SUPER_ADMIN_IDENTIFIER)) {
                    throw new BusinessException("权限不足，无法为指定用户解除管理员的绑定");
                }

                userRoleDao.delete(userId, roleId);
                log.info("unbind: (userId={}, roleId={})", userId, roleId);
            } catch (Exception e) {
                log.info(e.getMessage());
                failList.add(roleId);
            }
        }
        log.info("解除绑定失败的roleId：{}", failList);

        if (failList.size() < roleIds.size()) {
            log.info("有解除绑定成功，为用户标记须更新角色和权限标识符缓存");
            roleService.setRefreshRoleRedisTrue(userId);
            authService.setRefreshAuthRedisTrue(userId);
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
    public void changePassword(Long id, String password) {
        log.info("输入参数: id={}", id);

        String salt = PasswordUtil.generateSalt();
        String encryptedPassword = PasswordUtil.encode(password, salt);
        log.info("密码加密完成");

        User user = new User();
        user.setPassword(encryptedPassword);
        user.setSalt(salt);

        if (userDao.update(id, user) == 1) {
            log.info("创建用户成功完成");
        } else {
            throw new BusinessException("修改密码失败");
        }
    }

    private void updateRedisProfile(User profile) {
        StpUtil.getSession().set(RedisConst.PROFILE, profile);
        log.info("已更新缓存的profile信息: {}", profile);
    }

    @Override
    public User verifyByUsername(String username, String password) {
        log.info("输入参数: username={}", username);

        User user = userDao.findByUsername(username);
        if (user == null || !user.getPassword().equals(PasswordUtil.encode(password, user.getSalt()))) {
            throw new BusinessException("用户名或密码错误");
        }
        log.info("用户名和密码通过验证");

        return user;
    }

    @Override
    public User verifyById(Long id, String password) {
        log.info("输入参数: id={}", id);

        User user = userDao.findById(id);
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
