package run.wyatt.oneplatform.system.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import run.wyatt.oneplatform.system.service.AuthService;
import run.wyatt.oneplatform.system.service.RoleService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/16 17:10
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthService authService;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return roleService.getActivatedRoleIdentifiers(Long.valueOf(String.valueOf(loginId)));
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return authService.getActivatedAuthIdentifiers(Long.valueOf(String.valueOf(loginId)));
    }
}
