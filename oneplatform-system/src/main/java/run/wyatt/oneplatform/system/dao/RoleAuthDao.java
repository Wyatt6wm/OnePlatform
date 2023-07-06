package run.wyatt.oneplatform.system.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author Wyatt
 * @date 2023/6/21 11:41
 */
@Mapper
public interface RoleAuthDao {
    long insert(Long roleId, Long authId);
    long delete(Long roleId, Long authId);
    long deleteByRoleId(Long roleId);
    long deleteByAuthId(Long authId);
}
