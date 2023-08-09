package run.wyatt.oneplatform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.model.constant.TodoConst;
import run.wyatt.oneplatform.model.constant.TodoLogConst;
import run.wyatt.oneplatform.model.entity.Todo;
import run.wyatt.oneplatform.model.entity.TodoLog;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.model.form.TodoForm;
import run.wyatt.oneplatform.model.http.MapData;
import run.wyatt.oneplatform.model.http.R;
import run.wyatt.oneplatform.service.TodoLogService;
import run.wyatt.oneplatform.service.TodoService;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/17 15:36
 */
@Slf4j
@Api(tags = "待办接口")
@RestController
@RequestMapping("/api/todo")
public class TodoController {
    @Autowired
    private TodoService todoService;
    @Autowired
    private TodoLogService todoLogService;

    @ApiOperation("保存草稿")
    @SaCheckLogin
    @PostMapping("/saveDraft")
    public R saveDraft(@RequestBody TodoForm todoForm) {
        log.info("请求参数: {}", todoForm);
        Assert.notNull(todoForm, "todoForm为null");
        Assert.notNull(todoForm.getUuid(), "待办UUID为null");

        String status = todoForm.getStatus();
        if (!(status == null
                || status.isEmpty()
                || status.equals(TodoConst.STATUS_NEW)
                || status.equals(TodoConst.STATUS_DRAFT)
                || status.equals(TodoConst.STATUS_EDIT))) {
            log.info("当前状态为{}，无法保存草稿", status);
            throw new BusinessException("当前状态无法进行此操作");
        }

        Todo todo = todoForm.convert();
        if (status == null || status.isEmpty()) {
            todo.setStatus(TodoConst.STATUS_DRAFT);
        } else {
            switch (status) {
                case TodoConst.STATUS_NEW, TodoConst.STATUS_DRAFT ->
                        todo.setStatus(TodoConst.STATUS_DRAFT);                         // 新建、草稿状态保存为草稿
                case TodoConst.STATUS_EDIT -> todo.setStatus(TodoConst.STATUS_EDIT);    // 变更状态保存为变更
            }
        }
        log.info("组装后的待办: {}", todo);

        Long tenant = StpUtil.getLoginIdAsLong();
        try {
            todoService.editTodo(tenant, todo.getUuid(), todo);
            log.info("已有草稿，更新草稿记录");
        } catch (BusinessException be) {
            todo = todoService.createTodo(tenant, todo);
            log.info("未有草稿，新增草稿记录");
        }

        MapData data = new MapData();
        data.put("todo", todo);
        return R.success(data);
    }

    @ApiOperation("提交待办")
    @SaCheckLogin
    @PostMapping("/submitTodo")
    public R submitTodo(@RequestBody TodoForm todoForm) {
        log.info("请求参数: {}", todoForm);
        Assert.notNull(todoForm, "todoForm为null");
        Assert.notNull(todoForm.getUuid(), "待办UUID为null");

        String status = todoForm.getStatus();
        if (!(status == null
                || status.isEmpty()
                || status.equals(TodoConst.STATUS_NEW)
                || status.equals(TodoConst.STATUS_DRAFT)
                || status.equals(TodoConst.STATUS_EDIT))) {
            log.info("当前状态为{}，无法保存草稿", status);
            throw new BusinessException("当前状态无法进行此操作");
        }

        // 组装对象
        Todo todo = todoForm.convert();
        if (status == null || status.isEmpty()) {
            todo.setSubmitTime(new Date());
            todo.setStatus(TodoConst.STATUS_SUBMIT);
        } else {
            switch (status) {
                case TodoConst.STATUS_NEW, TodoConst.STATUS_DRAFT -> {
                    todo.setSubmitTime(new Date());
                    todo.setStatus(TodoConst.STATUS_SUBMIT);
                }   // 新建、草稿状态提交为已提交
                case TodoConst.STATUS_EDIT -> todo.setStatus(TodoConst.STATUS_PROGRESS);    // 变更状态提交为进行中
            }
        }
        log.info("组装后的待办: {}", todo);

        Long tenant = StpUtil.getLoginIdAsLong();
        try {
            todoService.editTodo(tenant, todo.getUuid(), todo);
            log.info("已有草稿，已完成提交");
        } catch (BusinessException be) {
            todo = todoService.createTodo(tenant, todo);
            log.info("未有草稿，已完成提交");
        }

        // 新增待办进度（会自动更新待办lastLogUuid）
        String title = (status == null || status.isEmpty() || status.equals(TodoConst.STATUS_EDIT)) ? "变更完成" : "提交待办";
        String logContent = todoService.getSnapshot(todo);
        TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, todo.getUuid(), title, logContent, TodoLogConst.TYPE_SNAPSHOT);
        todo.setLastLogUuid(lastTodoLog.getUuid()); // 最新进度的UUID绑定到待办
        log.info("新增待办日志成功");

        // 查询该待办的进度日志列表
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todo.getUuid());
        log.info("获取待办日志列表成功");

        MapData data = new MapData();
        data.put("todo", todo);
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("删除草稿")
    @SaCheckLogin
    @GetMapping("/removeDraft/{uuid}")
    public R removeDraft(@PathVariable("uuid") String uuid) {
        log.info("请求参数: uuid={}", uuid);

        log.info("查询待办数据");
        Long tenant = StpUtil.getLoginIdAsLong();
        Todo nowTodo = todoService.getTodo(tenant, uuid);
        if (nowTodo == null) {
            throw new BusinessException("该待办不存在");
        } else {
            String status = nowTodo.getStatus();
            if (status != null && status.equals(TodoConst.STATUS_DRAFT)) {
                todoService.removeTodo(tenant, uuid);
                return R.success();
            } else {
                throw new BusinessException("非草稿状态无法删除草稿");
            }
        }
    }

    @ApiOperation("待办切换到开始状态")
    @SaCheckLogin
    @GetMapping("/toProgress/{uuid}")
    public R toProgress(@PathVariable("uuid") String uuid) {
        log.info("请求参数: uuid={}", uuid);

        log.info("查询待办数据");
        Long tenant = StpUtil.getLoginIdAsLong();
        Todo nowTodo = todoService.getTodo(tenant, uuid);
        if (nowTodo == null) {
            throw new BusinessException("该待办不存在");
        } else {
            String status = nowTodo.getStatus();
            if (status != null && status.equals(TodoConst.STATUS_SUBMIT)) {
                TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, uuid, "开始待办", null, TodoLogConst.TYPE_TEXT);
                log.info("新增待办日志成功");

                Todo todo = new Todo();
                todo.setBeginTime(new Date());
                todo.setStatus(TodoConst.STATUS_PROGRESS);
                todo.setLastLogUuid(lastTodoLog.getUuid());
                todoService.editTodo(tenant, uuid, todo);
                log.info("更新待办状态成功");

                // 查询该待办的进度日志列表
                List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, uuid);
                log.info("获取待办日志列表成功");

                MapData data = new MapData();
                data.put("todo", todo);
                data.put("todoLogList", todoLogList);
                return R.success(data);
            } else {
                throw new BusinessException("待办未提交，无法开始待办");
            }
        }
    }

    @ApiOperation("待办切换到变更状态")
    @SaCheckLogin
    @GetMapping("/toEdit/{uuid}")
    public R toEdit(@PathVariable("uuid") String uuid) {
        log.info("请求参数: uuid={}", uuid);

        log.info("查询待办数据");
        Long tenant = StpUtil.getLoginIdAsLong();
        Todo nowTodo = todoService.getTodo(tenant, uuid);
        if (nowTodo == null) {
            throw new BusinessException("该待办不存在");
        } else {
            String status = nowTodo.getStatus();
            if (status != null && (status.equals(TodoConst.STATUS_SUBMIT) || status.equals(TodoConst.STATUS_PROGRESS))) {
                TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, uuid, "变更待办", null, TodoLogConst.TYPE_TEXT);
                log.info("新增待办日志成功");

                Todo todo = new Todo();
                todo.setStatus(TodoConst.STATUS_EDIT);
                todo.setLastLogUuid(lastTodoLog.getUuid());
                todoService.editTodo(tenant, uuid, todo);
                log.info("更新待办状态成功");

                // 查询该待办的进度日志列表
                List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, uuid);
                log.info("获取待办日志列表成功");

                MapData data = new MapData();
                data.put("todo", todo);
                data.put("todoLogList", todoLogList);
                return R.success(data);
            } else {
                throw new BusinessException("当前状态无法变更待办");
            }
        }
    }

    @ApiOperation("完成待办")
    @SaCheckLogin
    @PostMapping("/finishTodo")
    public R finishTodo(@RequestBody TodoForm todoForm) {
        log.info("请求参数: {}", todoForm);
        Assert.notNull(todoForm, "todoForm为null");
        Assert.notNull(todoForm.getUuid(), "uuid为null");
        Assert.hasText(todoForm.getConclusion(), "conclusion为空");

        log.info("查询待办数据");
        String uuid = todoForm.getUuid();
        Long tenant = StpUtil.getLoginIdAsLong();
        Todo nowTodo = todoService.getTodo(tenant, uuid);
        if (nowTodo == null) {
            throw new BusinessException("该待办不存在");
        } else {
            String status = nowTodo.getStatus();
            if (status != null && status.equals(TodoConst.STATUS_PROGRESS)) {
                TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, uuid, "完成待办", null, TodoLogConst.TYPE_TEXT);
                log.info("新增待办日志成功");

                Todo todo = new Todo();
                todo.setFinishTime(new Date());
                todo.setStatus(TodoConst.STATUS_FINISH);
                todo.setConclusion(todoForm.getConclusion());
                todo.setLastLogUuid(lastTodoLog.getUuid());
                todoService.editTodo(tenant, uuid, todo);
                log.info("更新待办状态成功");

                // 查询该待办的进度日志列表
                List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, uuid);
                log.info("获取待办日志列表成功");

                MapData data = new MapData();
                data.put("todo", todo);
                data.put("todoLogList", todoLogList);
                return R.success(data);
            } else {
                throw new BusinessException("当前状态无法完成待办");
            }
        }
    }

    @ApiOperation("取消待办")
    @SaCheckLogin
    @PostMapping("/cancelTodo")
    public R cancelTodo(@RequestBody TodoForm todoForm) {
        log.info("请求参数: {}", todoForm);
        Assert.notNull(todoForm, "todoForm为null");
        Assert.notNull(todoForm.getUuid(), "uuid为null");
        Assert.hasText(todoForm.getConclusion(), "conclusion为空");

        log.info("查询待办数据");
        String uuid = todoForm.getUuid();
        Long tenant = StpUtil.getLoginIdAsLong();
        Todo nowTodo = todoService.getTodo(tenant, uuid);
        if (nowTodo == null) {
            throw new BusinessException("该待办不存在");
        } else {
            String status = nowTodo.getStatus();
            if (status != null && (status.equals(TodoConst.STATUS_SUBMIT) || status.equals(TodoConst.STATUS_EDIT) || status.equals(TodoConst.STATUS_PROGRESS))) {
                TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, uuid, "取消待办", null, TodoLogConst.TYPE_TEXT);
                log.info("新增待办日志成功");

                Todo todo = new Todo();
                todo.setStatus(TodoConst.STATUS_CANCEL);
                todo.setConclusion(todoForm.getConclusion());
                todo.setLastLogUuid(lastTodoLog.getUuid());
                todoService.editTodo(tenant, uuid, todo);
                log.info("更新待办状态成功");

                // 查询该待办的进度日志列表
                List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, uuid);
                log.info("获取待办日志列表成功");

                MapData data = new MapData();
                data.put("todo", todo);
                data.put("todoLogList", todoLogList);
                return R.success(data);
            } else {
                throw new BusinessException("当前状态无法完成待办");
            }
        }
    }

    @ApiOperation("获取待办列表")
    @SaCheckLogin
    @GetMapping("/getTodoList/{category}")
    public R getTodoList(@PathVariable("category") String category) {
        log.info("请求参数: category={}", category);

        log.info("获取待办列表");
        Long tenant = StpUtil.getLoginIdAsLong();
        List<Todo> todoList = todoService.getTodoList(tenant, category);

        log.info("获取待办列表的最新进度");
        Collection<String> lastLogUuids = new HashSet<>();
        for (Todo todo : todoList) {
            if (todo.getLastLogUuid() != null) {
                lastLogUuids.add(todo.getLastLogUuid());
            }
        }
        List<TodoLog> lastLogList = todoLogService.getTodoLogList(tenant, lastLogUuids);

        MapData data = new MapData();
        data.put("todoList", todoList);
        data.put("lastLogList", lastLogList);
        return R.success(data);
    }

    @ApiOperation("查询待办")
    @SaCheckLogin
    @GetMapping("/getTodo/{uuid}")
    public R getTodo(@PathVariable("uuid") String uuid) {
        log.info("请求参数: uuid={}", uuid);

        Long tenant = StpUtil.getLoginIdAsLong();
        Todo todo = todoService.getTodo(tenant, uuid);

        MapData data = new MapData();
        data.put("todo", todo);
        return R.success(data);
    }
}
