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
     * 查询所有权限
     *
     * @return 权限列表
     */
    List<Auth> listAllAuths();
}
