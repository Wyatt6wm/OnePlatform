package run.wyatt.oneplatform.todo.service;

import run.wyatt.oneplatform.todo.model.entity.TodoLog;

import java.util.Collection;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/18 12:54
 */
public interface TodoLogService {
    /**
     * 新增待办进度记录
     *
     * @param tenant 租户
     * @param todoId 待办ID
     * @param title  进度标题
     * @param log    进度内容
     * @return 新增的进度记录对象
     */
    TodoLog addTodoLog(Long tenant, Long todoId, String title, String log);

    /**
     * 根据进度ID批量获取待办进度列表
     *
     * @param tenant 租户
     * @param ids    进度ID
     * @return 进度列表
     */
    List<TodoLog> getTodoLogList(Long tenant, Collection<Long> ids);

    /**
     * 获取某个待办的所有进度列表，按提交时间倒序排序
     *
     * @param tenant 租户ID
     * @param todoId 待办ID
     * @return 进度列表
     */
    List<TodoLog> getTodoLogList(Long tenant, Long todoId);
}
