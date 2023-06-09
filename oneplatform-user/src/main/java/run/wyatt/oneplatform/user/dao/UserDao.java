package run.wyatt.oneplatform.user.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author Wyatt
 * @date 2023/6/9 11:06
 */
@Mapper
public interface UserDao {
    long count();
}
