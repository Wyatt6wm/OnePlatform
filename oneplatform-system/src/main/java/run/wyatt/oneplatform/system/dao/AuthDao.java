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

    List<Auth> findAll();

    List<Auth> findByUserId(Long userId);

    List<Auth> findByRoleId(Long roleId);

    long update(Long authId, Auth record);
}
