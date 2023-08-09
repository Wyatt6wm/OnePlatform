package run.wyatt.oneplatform.service;

import run.wyatt.oneplatform.model.entity.Todo;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/17 16:27
 */
public interface TodoService {
    /**
     * 创建待办
     *
     * @param tenant  租户
     * @param newTodo 待创建待办数据
     * @return 新创建的待办对象
     */
    Todo createTodo(Long tenant, Todo newTodo);

    /**
     * 删除待办
     *
     * @param tenant 租户
     * @param uuid   待删除待办UUID
     */
    void removeTodo(Long tenant, String uuid);

    /**
     * 修改待办
     *
     * @param tenant  租户
     * @param uuid    待修改待办UUID
     * @param newTodo 待修改待办数据
     */
    void editTodo(Long tenant, String uuid, Todo newTodo);

    /**
     * 获取租户某类别的待办列表
     *
     * @param tenant   租户
     * @param category 分类：work/daily
     * @return 待办列表
     */
    List<Todo> getTodoList(Long tenant, String category);

    /**
     * 查询某个待办
     *
     * @param tenant 租户
     * @param uuid   待办ID
     * @return 要查询的待办
     */
    Todo getTodo(Long tenant, String uuid);

    /**
     * 为某个待办生成快照文本
     *
     * @param t 待生成快照的待办
     * @return 快照文本
     */
    String getSnapshot(Todo t);
}
