package run.wyatt.oneplatform.todo.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.todo.model.entity.Todo;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/17 15:25
 */
@Mapper
public interface TodoDao {
    long insert(Todo record);

    long delete(Long tenant, Long id);

    long update(Long tenant, Long id, Todo record);

    Todo findById(Long tenant, Long id);

    List<Todo> findByCategory(Long tenant, String category);
}
