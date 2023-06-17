package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.system.model.entity.User;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/9 10:37
 */
public interface UserService {
    /**
     * 根据用户名查询数据库并校验用户名密码
     *
     * @param username 用户名
     * @param password 名（明文）
     * @return 若认证成功则返回用户信息
     */
    User verifyUserByUsername(String username, String password);

    /**
     * 获取用户所属的所有角色
     *
     * @param userId 用户ID
     * @return 角色标识符列表
     */
    List<String> getUserRoleIdentifiers(Long userId);

    /**
     * 获取用户所拥有的权限
     *
     * @param userId 用户ID
     * @return 权限标识符列表
     */
    List<String> getUserPermissionIdentifiers(Long userId);
}
