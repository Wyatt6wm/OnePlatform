package run.wyatt.oneplatform.system.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.system.model.entity.Role;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/13 12:38
 */
@Mapper
public interface RoleDao {
    List<Role> findByUserId(Long userId);

    List<Role> findValidByUserId(Long userId);  // 要求角色activated
}
