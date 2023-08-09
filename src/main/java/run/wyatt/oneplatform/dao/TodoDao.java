package run.wyatt.oneplatform.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.model.entity.Todo;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/17 15:25
 */
@Mapper
public interface TodoDao {
    long insert(Todo record);

    long delete(Long tenant, String uuid);

    long update(Long tenant, String uuid, Todo record);

    Todo findByUuid(Long tenant, String uuid);

    List<Todo> findByCategory(Long tenant, String category);
}
