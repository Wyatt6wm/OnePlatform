package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.entity.Role;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/7 17:41
 */
public interface RoleService {
    /**
     * 创建新角色
     *
     * @param role 输入的角色信息
     * @return 新角色对象
     */
    Role createRole(Role role);

    /**
     * 删除角色记录
     *
     * @param roleId 要删除的角色ID
     */
    void removeRole(Long roleId);

    /**
     * 根据角色ID更新角色记录
     *
     * @param roleId 要更新的角色ID
     * @param role   新的角色数据
     * @return 更新后的角色对象
     */
    Role updateRole(Long roleId, Role role);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> listAllRoles();
}
