package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.system.model.entity.User;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/9 10:37
 */
public interface UserService {
    /**
     * 检查用户名输入格式是否不正确
     *
     * @param username 输入的用户名
     * @return true 格式不正确 / false 格式正确
     */
    boolean invalidUsernameFormat(String username);

    /**
     * 检查密码输入格式是否不正确
     *
     * @param password 输入的密码
     * @return true 格式不正确 / false 格式正确
     */
    boolean invalidPasswordFormat(String password);

    /**
     * 创建用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户对象
     */
    User createUser(String username, String password);

    /**
     * 为用户绑定多个角色
     *
     * @param userId  用户ID
     * @param roleIds 待绑定的角色ID列表
     * @return 绑定失败的角色ID列表
     */
    List<Long> bind(Long userId, List<Long> roleIds);

    /**
     * 为用户解除绑定多个角色
     *
     * @param userId  用户ID
     * @param roleIds 待解除绑定的角色ID列表
     * @return 解除绑定失败的角色ID列表
     */
    List<Long> unbind(Long userId, List<Long> roleIds);

    /**
     * 修改用户信息
     *
     * @param userId  用户ID
     * @param profile 用户信息
     * @return 更新后的用户对象
     */
    User editProfile(Long userId, User profile);

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
    List<String> getRoleIdentifiersOfUser(Long userId);

    /**
     * 获取用户所拥有的权限标识符列表
     *
     * @param userId 用户ID
     * @return 权限标识符列表
     */
    List<String> getAuthIdentifiersOfUser(Long userId);

    /**
     * 查询所有用户（脱敏）
     *
     * @return 用户列表
     */
    List<User> listAllUsersDesensitized();
}
