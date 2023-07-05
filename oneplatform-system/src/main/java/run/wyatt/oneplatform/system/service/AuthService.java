package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.system.model.entity.Auth;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:50
 */
public interface AuthService {
    /**
     * 根据用户ID获取该用户所有权限的详细信息
     *
     * @param userId 用户ID
     * @return 权限详细信息列表
     */
    List<Auth> listAuthDetails(Long userId);

    /**
     * 更新指定权限ID的权限详细信息
     *
     * @param newAuth 新的权限详细信息，其中id必填
     * @return true 更新成功
     */
    boolean updateAuthDetail(Auth newAuth);

    /**
     * 创建新权限
     *
     * @param auth 输入的权限信息
     * @return 新权限的ID
     */
    Long createAuth(Auth auth);
}
