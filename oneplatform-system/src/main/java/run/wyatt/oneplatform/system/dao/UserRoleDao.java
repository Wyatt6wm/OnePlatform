package run.wyatt.oneplatform.system.dao;

/**
 * @author Wyatt
 * @date 2023/6/21 11:41
 */
public interface UserRoleDao {
    int insert(Long userId, Long roleId);
}
