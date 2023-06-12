package run.wyatt.oneplatform.user.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.user.model.entity.Permission;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:04
 */
@Mapper
public interface PermissionDao {
    List<Permission> findActiatedPermissionsByUserId(Long userId);
}
