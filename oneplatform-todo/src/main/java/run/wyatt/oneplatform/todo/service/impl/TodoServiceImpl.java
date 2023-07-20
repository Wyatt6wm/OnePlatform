package run.wyatt.oneplatform.todo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.todo.dao.TodoDao;
import run.wyatt.oneplatform.todo.model.entity.Todo;
import run.wyatt.oneplatform.todo.service.TodoService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/17 16:45
 */
@Slf4j
@Service
public class TodoServiceImpl implements TodoService {
    @Autowired
    private TodoDao todoDao;

    @Override
    public Todo createTodo(Long tenant, Todo newTodo) {
        log.info("输入参数: tenant={} newTodo={}", tenant, newTodo);

        newTodo.setId(null);
        newTodo.setTenant(tenant);
        if (todoDao.insert(newTodo) == 0) {
            throw new BusinessException("创建待办失败");
        }
        log.info("成功创建待办: todoId={}", newTodo.getId());

        return newTodo;
    }

    @Override
    public void removeTodo(Long tenant, Long todoId) {
        log.info("输入参数: tenant={}, todoId={}", tenant, todoId);

        if (todoDao.delete(tenant, todoId) == 0) {
            throw new BusinessException("该待办数据不存在");
        }
        log.info("成功删除待办记录");
    }

    @Override
    public void editTodo(Long tenant, Long todoId, Todo newTodo) {
        log.info("输入参数: tenant={}, todoId={}, newTodo={}", tenant, todoId, newTodo);

        if (todoDao.update(tenant, todoId, newTodo) == 0) {
            throw new BusinessException("编辑待办失败");
        }
        log.info("成功编辑待办");
    }

    @Override
    public List<Todo> getTodoList(Long tenant, String category) {
        log.info("输入参数: tenant={}, category={}", tenant, category);
        return todoDao.findByCategory(tenant, category);
    }

    @Override
    public Todo getTodo(Long tenant, Long todoId) {
        log.info("输入参数: tenant={}, todoId={}", tenant, todoId);
        return todoDao.findById(tenant, todoId);
    }
}
