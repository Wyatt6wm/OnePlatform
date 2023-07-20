package run.wyatt.oneplatform.todo.service;

import run.wyatt.oneplatform.todo.model.entity.Todo;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/17 16:27
 */
public interface TodoService {
    Todo createTodo(Long tenant, Todo todo);

    void removeTodo(Long tenant, Long todoId);

    void editTodo(Long tenant, Long todoId, Todo todo);

    List<Todo> getTodoList(Long tenant, String category);

    Todo getTodo(Long tenant, Long todoId);
}
