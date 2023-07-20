package run.wyatt.oneplatform.todo.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Wyatt
 * @date 2023/7/18 12:32
 */
@Data
public class TodoLog {
    private Long id;
    private Long tenant;
    private Long todoId;
    private String title;
    private String log;
    private Date submitTime;
}
