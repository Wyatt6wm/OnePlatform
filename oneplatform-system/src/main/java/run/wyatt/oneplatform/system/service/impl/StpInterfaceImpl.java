package run.wyatt.oneplatform.system.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import run.wyatt.oneplatform.common.cosnt.CacheConst;
import run.wyatt.oneplatform.system.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/16 17:10
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private UserService userService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 先查询Redis
        List<String> permissionList = (ArrayList<String>) StpUtil.getSession().get(CacheConst.PERMISSIONS_KEY);
        // 如果Redis查询不到则调用远程方法查询数据库
        if (permissionList == null) {
            permissionList = userService.getUserPermissionIdentifiers(Long.valueOf(String.valueOf(loginId)));
        }
        return permissionList;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = (ArrayList<String>) StpUtil.getSession().get(CacheConst.ROLES_KEY);
        if (roleList == null) {
            roleList = userService.getUserRoleIdentifiers(Long.valueOf(String.valueOf(loginId)));
        }
        return roleList;
    }
}
