package run.wyatt.oneplatform.system.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.system.model.entity.User;

/**
 * @author Wyatt
 * @date 2023/6/9 11:06
 */
@Mapper
public interface UserDao {
    long insert(User record);
    long update(Long userId, User record);
    User findById(Long id);
    User findByUsername(String username);
    User findByNickname(String nickname);
}
