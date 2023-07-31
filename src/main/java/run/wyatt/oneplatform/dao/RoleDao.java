package run.wyatt.oneplatform.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.model.entity.Role;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/13 12:38
 */
@Mapper
public interface RoleDao {
    long insert(Role record);

    long delete(Long roleId);

    long update(Long roleId, Role record);

    List<Role> findAll();

    List<Role> findByUserId(Long userId);

    List<Role> findActivatedByUserId(Long userId);  // 要求角色activated
}
