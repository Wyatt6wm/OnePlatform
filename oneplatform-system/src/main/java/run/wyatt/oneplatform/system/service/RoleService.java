package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.entity.Role;
import run.wyatt.oneplatform.system.model.form.GrantForm;

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
     * 授权（绑定roleId和authId）
     *
     * @param roleId  角色ID
     * @param authIds 该角色要绑定的权限ID列表
     * @return 授权失败的权限ID列表
     */
    List<Long> grant(Long roleId, List<Long> authIds);

    /**
     * 取消授权（解除绑定roleId和authId）
     *
     * @param roleId  角色ID
     * @param authIds 该角色要解除绑定的权限ID列表
     * @return 解除授权失败的权限ID列表
     */
    List<Long> disgrant(Long roleId, List<Long> authIds);

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
     * 数据库角色表变更时间更新到Redis
     * 角色本身及用户绑定角色的关系发生变化时都应调用
     */
    void updateRoleDbChanged();

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> listAllRoles();

    /**
     * 查询角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Auth> listRoleAuths(Long roleId);
}
