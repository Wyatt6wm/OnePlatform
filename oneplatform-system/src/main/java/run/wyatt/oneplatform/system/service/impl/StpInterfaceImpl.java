package run.wyatt.oneplatform.system.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import run.wyatt.oneplatform.system.dao.PermissionDao;
import run.wyatt.oneplatform.system.dao.RoleDao;
import run.wyatt.oneplatform.system.model.entity.Permission;
import run.wyatt.oneplatform.system.model.entity.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/13 11:37
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private PermissionDao permissionDao;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> permissionList = new ArrayList<>();

        List<Permission> permissions = permissionDao.findActivatedPermissionsByUserId(Long.valueOf((String) loginId));
        for (Permission p : permissions) {
            permissionList.add(p.getIdentifier());
        }

        return permissionList;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();

        List<Role> roles = roleDao.findActivatedRolesByUserId(Long.valueOf((String) loginId));
        for (Role r : roles) {
            roleList.add(r.getIdentifier());
        }

        return roleList;
    }
}
