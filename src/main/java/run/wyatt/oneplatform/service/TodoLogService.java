package run.wyatt.oneplatform.service;


import run.wyatt.oneplatform.model.entity.TodoLog;

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
     * @param tenant   租户
     * @param todoUuid 待办ID
     * @param title    进度标题
     * @param log      进度内容
     * @param logType  进度日志类型
     * @return 新增的进度记录对象
     */
    TodoLog addTodoLog(Long tenant, String todoUuid, String title, String log, String logType);

    /**
     * 根据进度UUID批量获取待办进度列表
     *
     * @param tenant 租户
     * @param uuids  进度UUID
     * @return 进度列表
     */
    List<TodoLog> getTodoLogList(Long tenant, Collection<String> uuids);

    /**
     * 获取某个待办的所有进度列表，按提交时间倒序排序
     *
     * @param tenant   租户ID
     * @param todoUuid 待办UUID
     * @return 进度列表
     */
    List<TodoLog> getTodoLogList(Long tenant, String todoUuid);
}
