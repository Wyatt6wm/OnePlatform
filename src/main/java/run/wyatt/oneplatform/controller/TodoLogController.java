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
import run.wyatt.oneplatform.model.entity.TodoLog;
import run.wyatt.oneplatform.model.form.TodoLogForm;
import run.wyatt.oneplatform.model.http.MapData;
import run.wyatt.oneplatform.model.http.R;
import run.wyatt.oneplatform.service.TodoLogService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/18 16:04
 */
@Slf4j
@Api(tags = "待办日志接口")
@RestController
@RequestMapping("/api/todo-log")
public class TodoLogController {
    @Autowired
    private TodoLogService todoLogService;

    @ApiOperation("新增进度跟踪记录")
    @SaCheckLogin
    @PostMapping("/addTodoLog")
    public R addTodoLog(@RequestBody TodoLogForm todoLogForm) {
        log.info("请求参数: {}", todoLogForm);
        Assert.notNull(todoLogForm, "todoLogForm为null");
        Assert.notNull(todoLogForm.getTodoUuid(), "todoUuid为null");
        Assert.hasText(todoLogForm.getTitle(), "title为空");

        Long tenant = StpUtil.getLoginIdAsLong();
        todoLogService.addTodoLog(tenant, todoLogForm.getTodoUuid(), todoLogForm.getTitle(), todoLogForm.getLog(), todoLogForm.getLogType());
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoLogForm.getTodoUuid());

        MapData data = new MapData();
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }

    @ApiOperation("获取待办日志列表")
    @SaCheckLogin
    @GetMapping("/getTodoLogList/{todoUuid}")
    public R getTodoLogList(@PathVariable("todoUuid") String todoUuid) {
        log.info("请求参数: todoUuid={}", todoUuid);

        Long tenant = StpUtil.getLoginIdAsLong();
        List<TodoLog> todoLogList = todoLogService.getTodoLogList(tenant, todoUuid);

        MapData data = new MapData();
        data.put("todoLogList", todoLogList);
        return R.success(data);
    }
}
