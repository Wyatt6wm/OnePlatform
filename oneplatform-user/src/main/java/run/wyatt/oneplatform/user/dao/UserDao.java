package run.wyatt.oneplatform.user.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.user.model.entity.Permission;
import run.wyatt.oneplatform.user.model.entity.User;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/9 11:06
 */
@Mapper
public interface UserDao {
    long count();
    User findByUsername(String username);
}
