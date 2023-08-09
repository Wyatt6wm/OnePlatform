package run.wyatt.oneplatform.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.dao.TodoDao;
import run.wyatt.oneplatform.model.entity.Todo;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.service.TodoService;

import java.text.SimpleDateFormat;
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

        newTodo.setTenant(tenant);
        if (todoDao.insert(newTodo) == 0) {
            throw new BusinessException("创建待办失败");
        }
        log.info("成功创建待办");
        return newTodo;
    }

    @Override
    public void removeTodo(Long tenant, String uuid) {
        log.info("输入参数: tenant={}, uuid={}", tenant, uuid);

        if (todoDao.delete(tenant, uuid) == 0) {
            throw new BusinessException("该待办不存在");
        }
        log.info("成功删除待办");
    }

    @Override
    public void editTodo(Long tenant, String uuid, Todo newTodo) {
        log.info("输入参数: tenant={}, uuid={}, newTodo={}", tenant, uuid, newTodo);

        if (todoDao.update(tenant, uuid, newTodo) == 0) {
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
    public Todo getTodo(Long tenant, String uuid) {
        log.info("输入参数: tenant={}, uuid={}", tenant, uuid);
        return todoDao.findByUuid(tenant, uuid);
    }

    @Override
    public String getSnapshot(Todo t) {
        log.info("输入参数: {}", t);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        String name = "【待办标题】 " + t.getName() + "<br>";
        String emergency = "【紧急度】 " + (t.getEmergency() ? "紧急" : "一般") + "<br>";
        String importance = "【重要性】 " + (t.getImportance() ? "重要" : "一般") + "<br>";
        String deadline = "【截止时间】 " + sdf.format(t.getDeadline()) + "<br>";
        String workloadHour = "【工作总量】 " + String.format("%.1f", t.getWorkloadHour()) + "小时<br>";
        String workloadDay = "【跨越天数】 " + t.getWorkloadDay() + "天<br>";
        return name + emergency + importance + deadline + workloadHour + workloadDay;
    }
}
