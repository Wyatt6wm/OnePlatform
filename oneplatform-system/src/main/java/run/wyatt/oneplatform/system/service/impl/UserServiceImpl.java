package run.wyatt.oneplatform.system.service.impl;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.common.util.LogUtil;
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
        LogUtil logUtil = new LogUtil("createUser");
        log.info(logUtil.serviceBeginDivider("创建用户"));
        log.info("输入参数: username[{}] password[*]", username);

        try {
            // 判断用户是否已被注册
            if (userDao.findByUsername(username) != null) {
                log.info(logUtil.serviceFailDivider("用户已注册"));
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
            Long userId = userDao.insert(record);
            log.info("用户成功注册到数据库: userId[{}]", userId);

            // 为用户绑定默认角色
            if (userId != null) {
                bindRole(userId, SysConst.DEFAULT_ROLE_ID);
            }

            log.info(logUtil.serviceSuccessDivider());
            return userId;
        } catch (Exception e) {
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new DatabaseException();
        }
    }

    @Override
    public boolean bindRole(Long userId, Long roleId) {
        LogUtil logUtil = new LogUtil("bindRole");
        log.info(logUtil.serviceBeginDivider("为用户绑定角色"));
        log.info("输入参数: userId[{}], roleId[{}]", userId, roleId);

        try {
            int affected = userRoleDao.insert(userId, roleId);

            log.info(logUtil.serviceSuccessDivider());
            return affected == 1;
        } catch (Exception e) {
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new DatabaseException();
        }
    }

    @Override
    public User verifyByUsername(String username, String password) {
        LogUtil logUtil = new LogUtil("verifyByUsername");
        log.info(logUtil.serviceBeginDivider("根据用户名认证"));

        try {
            User user = userDao.findByUsername(username);
            if (user == null || !user.getPassword().equals(PasswordUtil.encode(password, user.getSalt()))) {
                log.info(logUtil.serviceFailDivider("用户名或密码错误"));
                throw new BusinessException("用户名或密码错误");
            }

            log.info(logUtil.serviceSuccessDivider());
            return user;
        } catch (Exception e) {
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new DatabaseException();
        }
    }

    @Override
    public List<String> listActivatedRoleIdentifiers(Long userId) {
        LogUtil logUtil = new LogUtil("listActivatedRoleIdentifiers");
        log.info(logUtil.serviceBeginDivider("查询用户的角色标识符列表"));

        try {
            List<Role> roles = roleDao.findRolesByUserId(userId);
            List<String> activatedRoleIdentifiers = new ArrayList<>();
            for (Role role : roles) {
                if (role.getActivated()) {
                    activatedRoleIdentifiers.add(role.getIdentifier());
                }
            }
            log.info("activatedRoleIdentifiers[{}]", JSONObject.toJSONString(activatedRoleIdentifiers));

            log.info(logUtil.serviceSuccessDivider());
            return activatedRoleIdentifiers;
        } catch (Exception e) {
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new DatabaseException();
        }
    }

    @Override
    public List<String> listActivatedAuthIdentifiers(Long userId) {
        LogUtil logUtil = new LogUtil("listActivatedAuthIdentifiers");
        log.info(logUtil.serviceBeginDivider("查询用户的权限标识符列表"));

        try {
            List<Auth> auths = authDao.findAuthsByUserId(userId);
            List<String> activatedAuthIdentifiers = new ArrayList<>();
            for (Auth auth : auths) {
                if (auth.getActivated()) {
                    activatedAuthIdentifiers.add(auth.getIdentifier());
                }
            }
            log.info("activatedAuthIdentifiers[{}]", JSONObject.toJSONString(activatedAuthIdentifiers));

            log.info(logUtil.serviceSuccessDivider());
            return activatedAuthIdentifiers;
        } catch (Exception e) {
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new DatabaseException();
        }
    }
}
