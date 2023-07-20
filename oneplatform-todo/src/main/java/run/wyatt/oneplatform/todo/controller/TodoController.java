package run.wyatt.oneplatform.todo.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.http.MapData;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.todo.model.constant.TodoConst;
import run.wyatt.oneplatform.todo.model.entity.Todo;
import run.wyatt.oneplatform.todo.model.entity.TodoLog;
import run.wyatt.oneplatform.todo.model.form.TodoForm;
import run.wyatt.oneplatform.todo.service.TodoLogService;
import run.wyatt.oneplatform.todo.service.TodoService;

import java.text.SimpleDateFormat;
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

        // 租户
        Long tenant = StpUtil.getLoginIdAsLong();
        // 待办草稿数据
        Long id = todoForm.getId();
        String category = todoForm.getCategory();
        String name = todoForm.getName();
        Boolean emergency = todoForm.getEmergency();
        Boolean importance = todoForm.getImportance();
        Date deadline = todoForm.getDeadline();
        String detail = todoForm.getDetail();
        String workload = todoForm.getWorkload();
        Double workloadHour = todoForm.getWorkloadHour();
        Integer workloadDay = todoForm.getWorkloadDay();
        String status = todoForm.getStatus();

        Todo todo = new Todo();
        todo.setId(id);
        todo.setCategory(category);
        if (name != null && !name.isBlank()) todo.setName(name);
        todo.setEmergency(emergency);
        todo.setImportance(importance);
        todo.setDeadline(deadline);
        if (detail != null && !detail.isBlank()) todo.setDetail(detail);
        if (workload != null && !workload.isBlank()) todo.setWorkload(workload);
        todo.setWorkloadHour(workloadHour);
        todo.setWorkloadDay(workloadDay);
        switch (status) {
            // 新建、草稿状态保存为草稿
            case TodoConst.STATUS_NEW, TodoConst.STATUS_DRAFT -> todo.setStatus(TodoConst.STATUS_DRAFT);
            // 变更状态保存为变更
            case TodoConst.STATUS_EDIT -> todo.setStatus(TodoConst.STATUS_EDIT);
        }
        log.info("组装后的待办: {}", todo);

        if (todo.getId() == null) {
            log.info("未有草稿，新增草稿记录");
            todo = todoService.createTodo(tenant, todo);
        } else {
            log.info("已有草稿，更新草稿记录");
            todoService.editTodo(tenant, todo.getId(), todo);
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

        // 租户
        Long tenant = StpUtil.getLoginIdAsLong();
        // 待办数据
        Long id = todoForm.getId();
        String category = todoForm.getCategory();
        String name = todoForm.getName();
        Boolean emergency = todoForm.getEmergency();
        Boolean importance = todoForm.getImportance();
        Date deadline = todoForm.getDeadline();
        String detail = todoForm.getDetail();
        String workload = todoForm.getWorkload();
        Double workloadHour = todoForm.getWorkloadHour();
        Integer workloadDay = todoForm.getWorkloadDay();
        String status = todoForm.getStatus();

        // 组装对象
        Todo todo = new Todo();
        todo.setId(id);
        todo.setTenant(StpUtil.getLoginIdAsLong());
        todo.setCategory(category);
        if (name != null && !name.isBlank()) todo.setName(name);
        todo.setEmergency(emergency);
        todo.setImportance(importance);
        todo.setDeadline(deadline);
        if (detail != null && !detail.isBlank()) todo.setDetail(detail);
        if (workload != null && !workload.isBlank()) todo.setWorkload(workload);
        todo.setWorkloadHour(workloadHour);
        todo.setWorkloadDay(workloadDay);
        switch (status) {
            // 新建、草稿状态提交为已提交
            case TodoConst.STATUS_NEW, TodoConst.STATUS_DRAFT -> {
                todo.setSubmitTime(new Date());
                todo.setStatus(TodoConst.STATUS_SUBMIT);
            }
            // 变更状态提交为进行中
            case TodoConst.STATUS_EDIT -> todo.setStatus(TodoConst.STATUS_PROGRESS);
        }
        log.info("组装后的待办: {}", todo);

        // 如果是新增，则创建数据库记录
        if (todo.getId() == null) {
            todo = todoService.createTodo(tenant, todo);
            log.info("成功提交待办（从草稿状态提交）");
        } else {
            todoService.editTodo(tenant, todo.getId(), todo);
            log.info("成功提交待办（从草稿状态提交）");
        }

        // 新增待办进度（会自动更新待办lastLogId）
        String logTitle = status.equals(TodoConst.STATUS_EDIT) ? "变更完成" : "提交待办";
        TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, todo.getId(), logTitle, formattedLog(todo));
        todo.setLastLogId(lastTodoLog.getId()); // 最新进度的ID绑定到待办
        log.info("新增待办日志成功");

        // 查询该待办的进度日志列表
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todo.getId());
        log.info("获取待办日志列表成功");

        MapData data = new MapData();
        data.put("todo", todo);
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("删除草稿")
    @SaCheckLogin
    @GetMapping("/removeDraft")
    public R removeDraft(@RequestParam(value = "todoId") Long todoId) {
        log.info("请求参数: todoId={}", todoId);

        log.info("删除待办草稿");
        Long tenant = StpUtil.getLoginIdAsLong();
        todoService.removeTodo(tenant, todoId);
        return R.success();
    }

    @ApiOperation("待办切换到开始状态")
    @SaCheckLogin
    @GetMapping("/toProgress")
    public R toProgress(@RequestParam("todoId") Long todoId) {
        Long tenant = StpUtil.getLoginIdAsLong();

        TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, todoId, "开始待办", null);
        log.info("新增待办日志成功");

        Todo todo = new Todo();
        todo.setBeginTime(new Date());
        todo.setStatus(TodoConst.STATUS_PROGRESS);
        todo.setLastLogId(lastTodoLog.getId());
        todoService.editTodo(tenant, todoId, todo);
        log.info("更新待办状态成功");

        // 查询该待办的进度日志列表
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoId);
        log.info("获取待办日志列表成功");

        MapData data = new MapData();
        data.put("todo", todo);
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("待办切换到变更状态")
    @SaCheckLogin
    @GetMapping("/toEdit")
    public R toEdit(@RequestParam("todoId") Long todoId) {
        Long tenant = StpUtil.getLoginIdAsLong();

        TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, todoId, "变更待办", null);
        log.info("新增待办日志成功");

        Todo todo = new Todo();
        todo.setStatus(TodoConst.STATUS_EDIT);
        todo.setLastLogId(lastTodoLog.getId());
        todoService.editTodo(tenant, todoId, todo);
        log.info("更新待办状态成功");

        // 查询该待办的进度日志列表
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoId);
        log.info("获取待办日志列表成功");

        MapData data = new MapData();
        data.put("todo", todo);
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("完成待办")
    @SaCheckLogin
    @PostMapping("/finishTodo")
    public R finishTodo(@RequestBody TodoForm todoForm) {
        log.info("请求参数: {}", todoForm);
        Assert.notNull(todoForm, "todoForm为null");
        Assert.notNull(todoForm.getId(), "todoId为null");
        Assert.hasText(todoForm.getConclusion(), "conclusion为null");

        Long tenant = StpUtil.getLoginIdAsLong();

        TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, todoForm.getId(), "完成待办", todoForm.getConclusion());

        Todo todo = new Todo();
        todo.setFinishTime(new Date());
        todo.setStatus(TodoConst.STATUS_FINISH);
        todo.setConclusion(todoForm.getConclusion());
        todo.setLastLogId(lastTodoLog.getId());
        todoService.editTodo(tenant, todoForm.getId(), todo);

        // 查询该待办的进度日志列表
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoForm.getId());
        log.info("获取待办日志列表成功");

        MapData data = new MapData();
        data.put("todo", todo);
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("取消待办")
    @SaCheckLogin
    @PostMapping("/cancelTodo")
    public R cancelTodo(@RequestBody TodoForm todoForm) {
        log.info("请求参数: {}", todoForm);
        Assert.notNull(todoForm, "todoForm为null");
        Assert.notNull(todoForm.getId(), "todoId为null");
        Assert.hasText(todoForm.getConclusion(), "conclusion为null");

        Long tenant = StpUtil.getLoginIdAsLong();

        TodoLog lastTodoLog = todoLogService.addTodoLog(tenant, todoForm.getId(), "取消待办", todoForm.getConclusion());

        Todo todo = new Todo();
        todo.setStatus(TodoConst.STATUS_CANCEL);
        todo.setConclusion(todoForm.getConclusion());
        todo.setLastLogId(lastTodoLog.getId());
        todoService.editTodo(tenant, todoForm.getId(), todo);

        // 查询该待办的进度日志列表
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoForm.getId());
        log.info("获取待办日志列表成功");

        MapData data = new MapData();
        data.put("todo", todo);
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("获取待办列表")
    @SaCheckLogin
    @GetMapping("/getTodoList")
    public R getTodoList(@RequestParam(value = "category") String category) {
        log.info("请求参数: category={}", category);

        log.info("获取待办列表");
        Long tenant = StpUtil.getLoginIdAsLong();
        List<Todo> todoList = todoService.getTodoList(tenant, category);

        log.info("获取待办列表的最新进度");
        Collection<Long> lastLogIds = new HashSet<>();
        for (Todo todo : todoList) {
            if (todo.getLastLogId() != null) {
                lastLogIds.add(todo.getLastLogId());
            }
        }
        List<TodoLog> lastLogList = todoLogService.getTodoLogList(tenant, lastLogIds);

        MapData data = new MapData();
        data.put("todoList", todoList);
        data.put("lastLogList", lastLogList);
        return R.success(data);
    }

    @ApiOperation("查询待办")
    @SaCheckLogin
    @GetMapping("/getTodo")
    public R getTodo(@RequestParam("todoId") Long todoId) {
        log.info("请求参数: todoId={}", todoId);

        Long tenant = StpUtil.getLoginIdAsLong();
        Todo todo = todoService.getTodo(tenant, todoId);

        MapData data = new MapData();
        data.put("todo", todo);
        return R.success(data);
    }

    private String formattedLog(Todo todo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        String name = "【待办标题】 " + todo.getName() + "<br>";
        String emergency = "【紧急度】 " + (todo.getEmergency() ? "紧急" : "一般") + "<br>";
        String importance = "【重要性】 " + (todo.getImportance() ? "重要" : "一般") + "<br>";
        String deadline = "【截止时间】 " + sdf.format(todo.getDeadline()) + "<br>";
        String workloadHour = "【工作总量】 " + sdf.format(todo.getWorkloadHour()) + "小时<br>";
        String workloadDay = "【跨越天数】 " + sdf.format(todo.getDeadline()) + "天<br>";
        return name + emergency + importance + deadline + workloadHour + workloadDay;
    }
}
