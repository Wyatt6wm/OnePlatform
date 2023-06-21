package run.wyatt.oneplatform.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
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
@Service
public class UserServiceImpl implements UserService {
    private static final String USERNAME_REGEXP = "^[A-Za-z0-9]{1,16}$";
    private static final String PASSWORD_REGEXP = "^[A-Za-z0-9.~!@#$%^&*_?]{6,16}$";

    @Autowired
    private UserDao userDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private AuthDao authDao;

    @Override
    public boolean checkUsernameFormat(String username) {
        return username.matches(USERNAME_REGEXP);
    }

    @Override
    public boolean checkPasswordFormat(String password) {
        return password.matches(PASSWORD_REGEXP);
    }

    @Override
    public Long createUser(String username, String password) {
        try {
            // 判断用户是否已被注册
            if (userDao.findByUsername(username) != null) {
                throw new BusinessException("用户已注册");
            }

            // 密码加密
            String salt = PasswordUtil.generateSalt();
            String encryptedPassword = PasswordUtil.encode(password, salt);

            // 保存到数据库
            User record = new User();
            record.setUsername(username);
            record.setPassword(encryptedPassword);
            record.setSalt(salt);
            Long userId = userDao.insert(record);

            // 为用户绑定默认角色
            if (userId != null) {
                bindRole(userId, SysConst.DEFAULT_ROLE_ID);
            }

            return userId;
        } catch (Exception e) {
            throw new DatabaseException();
        }
    }

    @Override
    public boolean bindRole(Long userId, Long roleId) {
        try {
            int affected = userRoleDao.insert(userId, roleId);
            return affected == 1;
        } catch (Exception e) {
            throw new DatabaseException();
        }
    }

    @Override
    public User verifyByUsername(String username, String password) {
        try {
            User user = userDao.findByUsername(username);
            if (user == null || !user.getPassword().equals(PasswordUtil.encode(password, user.getSalt()))) {
                throw new BusinessException("用户名或密码错误");
            }
            return user;
        } catch (Exception e) {
            throw new DatabaseException();
        }
    }

    @Override
    public List<String> listActivatedRoleIdentifiers(Long userId) {
        try {
            List<Role> roles = roleDao.findRolesByUserId(userId);
            List<String> activatedRoleIdentifiers = new ArrayList<>();
            for (Role role : roles) {
                if (role.getActivated()) {
                    activatedRoleIdentifiers.add(role.getIdentifier());
                }
            }
            return activatedRoleIdentifiers;
        } catch (Exception e) {
            throw new DatabaseException();
        }
    }

    @Override
    public List<String> listActivatedAuthIdentifiers(Long userId) {
        List<String> permissionList = new ArrayList<>();
        try {
            List<Auth> auths = authDao.findAuthsByUserId(userId);
            List<String> activatedAuthIdentifiers = new ArrayList<>();
            for (Auth auth : auths) {
                if (auth.getActivated()) {
                    activatedAuthIdentifiers.add(auth.getIdentifier());
                }
            }
            return activatedAuthIdentifiers;
        } catch (Exception e) {
            throw new DatabaseException();
        }
    }
}
