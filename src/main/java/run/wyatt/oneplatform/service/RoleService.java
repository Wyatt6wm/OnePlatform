package run.wyatt.oneplatform.service;


import run.wyatt.oneplatform.model.entity.Role;

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
     * @param roleId 角色ID
     * @param authId 该角色要绑定的权限ID
     */
    void grant(Long roleId, Long authId);

    /**
     * 授权（绑定roleId和authId列表）
     *
     * @param roleId  角色ID
     * @param authIds 该角色要绑定的权限ID列表
     * @return 授权失败的权限ID列表
     */
    List<Long> grant(Long roleId, List<Long> authIds);

    /**
     * 取消授权（解除绑定roleId和authId列表）
     *
     * @param roleId  角色ID
     * @param authIds 该角色要解除绑定的权限ID列表
     * @return 解除授权失败的权限ID列表
     */
    List<Long> ungrant(Long roleId, List<Long> authIds);

    /**
     * 删除角色记录
     *
     * @param roleId 要删除的角色ID
     */
    void removeRole(Long roleId);

    /**
     * 根据角色ID更新角色记录
     *
     * @param role 新的角色数据
     * @return 更新后的角色对象
     */
    Role updateRole(Role role);

    /**
     * 数据库角色表变更时间更新到Redis
     * 角色本身及用户绑定角色的关系发生变化时都应调用
     */
    void updateRoleDbChangeTime();

    /**
     * Redis缓存中的角色更新时间戳
     * 当有更新Redis缓存中的角色时都应调用
     */
    void updateRoleRedisChangeTime();

    void setRefreshRoleRedisTrue(Long userId);

    void setRefreshRoleRedisFalse(Long userId);

    /**
     * 查询用户所有正在生效的角色标识
     *
     * @param userId 用户ID
     * @return 角色标识符列表
     */
    List<String> getActivatedRoleIdentifiers(Long userId);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> listRoles();

    /**
     * 查询用户所有角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> listRoles(Long userId);
}
