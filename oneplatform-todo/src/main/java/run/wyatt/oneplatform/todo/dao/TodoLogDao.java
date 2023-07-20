package run.wyatt.oneplatform.todo.dao;

import org.apache.ibatis.annotations.Mapper;
import run.wyatt.oneplatform.todo.model.entity.TodoLog;

import java.util.Collection;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/18 12:34
 */
@Mapper
public interface TodoLogDao {
    long insert(TodoLog record);
    List<TodoLog> findByIds(Long tenant, Collection<Long> ids);
    List<TodoLog> findByTodoIdSortBySubmitTimeDesc(Long tenant, Long todoId);
}
