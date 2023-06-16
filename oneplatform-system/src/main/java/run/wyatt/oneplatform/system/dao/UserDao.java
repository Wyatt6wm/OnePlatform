package run.wyatt.oneplatform.system.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.system.model.entity.User;

/**
 * @author Wyatt
 * @date 2023/6/9 11:06
 */
@Mapper
public interface UserDao {
    long count();
    User findByUsername(String username);
}
