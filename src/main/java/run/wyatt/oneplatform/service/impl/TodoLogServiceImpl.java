package run.wyatt.oneplatform.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.dao.TodoDao;
import run.wyatt.oneplatform.dao.TodoLogDao;
import run.wyatt.oneplatform.model.entity.Todo;
import run.wyatt.oneplatform.model.entity.TodoLog;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.service.TodoLogService;
import run.wyatt.oneplatform.util.UuidUtil;

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
    public TodoLog addTodoLog(Long tenant, String todoUuid, String title, String logContent, String logType) {
        log.info("输入参数: tenant={}, todoUuid={}, title={}, logContent={}", tenant, todoUuid, title, logContent);

        // 创建日志记录
        TodoLog todoLog = new TodoLog();
        todoLog.setUuid(UuidUtil.generateUuid());
        todoLog.setTenant(tenant);
        todoLog.setTodoUuid(todoUuid);
        todoLog.setTitle(title);
        todoLog.setLog(logContent);
        todoLog.setLogType(logType);
        todoLog.setSubmitTime(new Date());
        if (todoLogDao.insert(todoLog) == 0) {
            throw new BusinessException("创建待办日志失败");
        }
        log.info("成功创建待办日志: uuid={}", todoLog.getUuid());

        // 更新对应待办的lastLogUuid
        Todo todo = new Todo();
        todo.setLastLogUuid(todoLog.getUuid());
        if (todoDao.update(tenant, todoUuid, todo) == 0) {
            throw new BusinessException("更新待办lastLogUuid失败");
        }
        log.info("成功更新待办lastLogUuid");

        return todoLog;
    }

    @Override
    public List<TodoLog> getTodoLogList(Long tenant, Collection<String> uuids) {
        log.info("输入参数: tenant={}, uuids={}", tenant, uuids);
        return todoLogDao.findByUuids(tenant, uuids);
    }

    @Override
    public List<TodoLog> getTodoLogList(Long tenant, String todoUuid) {
        log.info("输入参数: tenant={}, todoUuid={}", tenant, todoUuid);
        return todoLogDao.findByTodoUuidSortBySubmitTimeDesc(tenant, todoUuid);
    }
}
