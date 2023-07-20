package run.wyatt.oneplatform.todo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.todo.dao.TodoDao;
import run.wyatt.oneplatform.todo.dao.TodoLogDao;
import run.wyatt.oneplatform.todo.model.entity.Todo;
import run.wyatt.oneplatform.todo.model.entity.TodoLog;
import run.wyatt.oneplatform.todo.service.TodoLogService;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/18 12:55
 */
@Slf4j
@Service
public class TodoLogServiceImpl implements TodoLogService {
    @Autowired
    private TodoDao todoDao;
    @Autowired
    private TodoLogDao todoLogDao;

    @Override
    public TodoLog addTodoLog(Long tenant, Long todoId, String title, String logContent) {
        log.info("输入参数: tenant={}, todoId={}, title={}, logContent={}", tenant, todoId, title, logContent);

        // 创建日志记录
        TodoLog todoLog = new TodoLog();
        todoLog.setId(null);
        todoLog.setTenant(tenant);
        todoLog.setTodoId(todoId);
        todoLog.setTitle(title);
        todoLog.setLog(logContent);
        todoLog.setSubmitTime(new Date());
        if (todoLogDao.insert(todoLog) == 0) {
            throw new BusinessException("创建待办日志失败");
        }
        log.info("成功创建待办日志: todoLogId={}", todoLog.getId());

        // 更新对应待办的lastLogId
        Todo todo = new Todo();
        todo.setLastLogId(todoLog.getId());
        if (todoDao.update(tenant, todoId, todo) == 0) {
            throw new BusinessException("更新待办lastLogId失败");
        }
        log.info("成功更新待办lastLogId");

        return todoLog;
    }

    @Override
    public List<TodoLog> getTodoLogList(Long tenant, Collection<Long> ids) {
        log.info("输入参数: tenant={}, ids={}", tenant, ids);
        return todoLogDao.findByIds(tenant, ids);
    }

    @Override
    public List<TodoLog> getTodoLogList(Long tenant, Long todoId) {
        log.info("输入参数: tenant={}, todoId={}", tenant, todoId);
        return todoLogDao.findByTodoIdSortBySubmitTimeDesc(tenant, todoId);
    }
}
