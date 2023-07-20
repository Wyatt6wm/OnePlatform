package run.wyatt.oneplatform.todo.model.form;

import lombok.Data;

import java.util.Date;

/**
 * @author Wyatt
 * @date 2023/7/20 10:26
 */
@Data
public class TodoLogForm {
    private Long id;
    private Long tenant;
    private Long todoId;
    private String title;
    private String log;
    private Date submitTime;
}
