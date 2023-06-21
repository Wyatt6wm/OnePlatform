package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.system.model.entity.User;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/9 10:37
 */
public interface UserService {
    /**
     * 检查用户名输入格式是否正确
     *
     * @param username 输入的用户名
     * @return true / false
     */
    boolean checkUsernameFormat(String username);

    /**
     * 检查密码输入格式是否正确
     *
     * @param password 输入的密码
     * @return true / false
     */
    boolean checkPasswordFormat(String password);

    /**
     * 创建用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户ID
     */
    Long createUser(String username, String password);

    /**
     * 为用户绑定角色
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return true / false
     */
    boolean bindRole(Long userId, Long roleId);

    /**
     * 根据用户名查询数据库并校验用户名密码
     *
     * @param username 用户名
     * @param password 名（明文）
     * @return 若认证成功则返回用户信息
     */
    User verifyByUsername(String username, String password);

    /**
     * 获取用户所属的所有角色标识符列表
     *
     * @param userId 用户ID
     * @return 角色标识符列表
     */
    List<String> listActivatedRoleIdentifiers(Long userId);

    /**
     * 获取用户所拥有的权限标识符列表
     *
     * @param userId 用户ID
     * @return 权限标识符列表
     */
    List<String> listActivatedAuthIdentifiers(Long userId);
}
