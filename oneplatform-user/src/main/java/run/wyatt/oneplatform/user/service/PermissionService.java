package run.wyatt.oneplatform.user.service;

import run.wyatt.oneplatform.user.model.entity.Permission;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:50
 */
public interface PermissionService {
    List<Permission> getPermissionsByUserId(Long userId);
}
