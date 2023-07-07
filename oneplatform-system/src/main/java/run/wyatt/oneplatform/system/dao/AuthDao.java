package run.wyatt.oneplatform.system.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.system.model.entity.Auth;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:04
 */
@Mapper
public interface AuthDao {
    long insert(Auth record);

    long delete(Long authId);

    long update(Long authId, Auth record);

    List<Auth> findAll();

    List<Auth> findByUserId(Long userId);

    List<Auth> findValidByUserId(Long userId);  // 要求满足角色activated且权限activated

    List<Auth> findByRoleId(Long roleId);
}
