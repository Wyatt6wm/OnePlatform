package run.wyatt.oneplatform.todo.service;

import run.wyatt.oneplatform.todo.model.entity.TodoLog;

import java.util.Collection;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/18 12:54
 */
public interface TodoLogService {
    TodoLog addTodoLog(Long tenant, Long todoId, String title, String log);

    List<TodoLog> getTodoLogList(Long tenant, Collection<Long> ids);

    List<TodoLog> getTodoLogList(Long tenant, Long todoId);
}
