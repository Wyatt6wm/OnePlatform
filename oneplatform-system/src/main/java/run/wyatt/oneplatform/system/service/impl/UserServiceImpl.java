package run.wyatt.oneplatform.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import run.wyatt.oneplatform.common.utils.PasswordUtils;
import run.wyatt.oneplatform.system.dao.PermissionDao;
import run.wyatt.oneplatform.system.dao.RoleDao;
import run.wyatt.oneplatform.system.dao.UserDao;
import run.wyatt.oneplatform.system.model.entity.Permission;
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
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private PermissionDao permissionDao;

    @Override
    public User verifyUserByUsername(String username, String password) {
        User user = null;

        try {
            user = userDao.findByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("数据库错误");
        }

        if (user == null || !user.getPassword().equals(PasswordUtils.encode(password, user.getSalt()))) {
            throw new RuntimeException("用户名或密码错误");
        }

        // TODO 检查账号状态

        return user;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<String> getUserRoleIdentifiers(Long userId) {
        List<String> roleList = new ArrayList<>();

        List<Role> roles = roleDao.findActivatedRolesByUserId(userId);
        for (Role r : roles) {
            roleList.add(r.getIdentifier());
        }

        return roleList;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<String> getUserPermissionIdentifiers(Long userId) {
        List<String> permissionList = new ArrayList<>();

        List<Permission> permissions = permissionDao.findActivatedPermissionsByUserId(userId);
        for (Permission p : permissions) {
            permissionList.add(p.getIdentifier());
        }

        return permissionList;
    }
}
