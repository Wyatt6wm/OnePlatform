package run.wyatt.oneplatform.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.model.entity.Auth;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:04
 */
@Mapper
public interface AuthDao {
    long insert(Auth record);

    long delete(Long id);

    long update(Long id, Auth record);

    List<Auth> findAll();

    List<Auth> findByRoleId(Long roleId);

    List<Auth> findActivatedByUserId(Long userId);  // 要求满足角色activated且权限activated
}
