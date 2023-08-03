package run.wyatt.oneplatform.dao;

/**
 * @author Wyatt
 * @date 2023/6/21 11:41
 */
public interface UserRoleDao {
    long insert(Long userId, Long roleId);

    long delete(Long userId, Long roleId);

    long deleteByUserId(Long userId);

    long deleteByRoleId(Long roleId);
}
