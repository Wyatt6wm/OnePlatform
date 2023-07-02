package run.wyatt.oneplatform.system.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import run.wyatt.oneplatform.system.model.entity.Auth;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:04
 */
@Mapper
public interface AuthDao {
    List<Auth> findAuthsByUserId(Long userId);
    int update(@Param("auth") Auth auth);
}
