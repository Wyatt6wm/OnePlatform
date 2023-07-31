package run.wyatt.oneplatform.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.model.entity.User;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/9 11:06
 */
@Mapper
public interface UserDao {
    long insert(User record);

    long update(Long userId, User record);

    List<User> findAll();

    User findById(Long id);

    User findByUsername(String username);

    User findByNickname(String nickname);
}
