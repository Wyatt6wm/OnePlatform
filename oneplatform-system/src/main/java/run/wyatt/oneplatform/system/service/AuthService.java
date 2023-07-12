package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.system.model.entity.Auth;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:50
 */
public interface AuthService {
    /**
     * 创建新权限
     *
     * @param auth 输入的权限信息
     * @return 新权限对象
     */
    Auth createAuth(Auth auth);

    /**
     * 删除权限记录
     *
     * @param authId 要删除的权限ID
     */
    void removeAuth(Long authId);

    /**
     * 根据权限ID更新权限记录
     *
     * @param authId 要更新的权限ID
     * @param auth   新的权限数据
     * @return 更新后的权限对象
     */
    Auth updateAuth(Long authId, Auth auth);

    /**
     * 数据库权限表变更时间更新到Redis
     * 权限本身及角色绑定权限的关系发生变化时都应调用
     */
    void updateAuthDbChangeTime();

    void setRefreshAuthRedisTrue(Long userId);

    void setRefreshAuthRedisFalse(Long userId);

    /**
     * 查询用户所有正在生效的权限标识
     *
     * @param userId 用户ID
     * @return 权限标识符列表
     */
    List<String> getActivatedAuthIdentifiers(Long userId);

    /**
     * 查询所有权限
     *
     * @return 权限列表
     */
    List<Auth> listAuths();

    /**
     * 查询角色的所有权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Auth> listAuths(Long roleId);
}
