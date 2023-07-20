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
import run.wyatt.oneplatform.todo.model.entity.TodoLog;
import run.wyatt.oneplatform.todo.model.form.TodoLogForm;
import run.wyatt.oneplatform.todo.service.TodoLogService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/18 16:04
 */
@Slf4j
@Api(tags = "待办日志接口")
@RestController
@RequestMapping("/api/todo/log")
public class TodoLogController {
    @Autowired
    private TodoLogService todoLogService;

    @ApiOperation("新增进度跟踪记录")
    @SaCheckLogin
    @PostMapping("/addTodoLog")
    public R addTodoLog(@RequestBody TodoLogForm todoLogForm) {
        log.info("请求参数: {}", todoLogForm);
        Assert.notNull(todoLogForm, "todoLogForm为null");
        Assert.notNull(todoLogForm.getTodoId(), "todoId为null");
        Assert.notNull(todoLogForm.getTitle(), "title为null");

        Long tenant = StpUtil.getLoginIdAsLong();
        todoLogService.addTodoLog(tenant, todoLogForm.getTodoId(), todoLogForm.getTitle(), todoLogForm.getLog());
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoLogForm.getTodoId());

        MapData data = new MapData();
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("获取待办日志列表")
    @SaCheckLogin
    @GetMapping("/getTodoLogList")
    public R getTodoLogList(@RequestParam("todoId") Long todoId) {
        log.info("请求参数: todoId={}", todoId);

        Long tenant = StpUtil.getLoginIdAsLong();
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoId);

        MapData data = new MapData();
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }
}
