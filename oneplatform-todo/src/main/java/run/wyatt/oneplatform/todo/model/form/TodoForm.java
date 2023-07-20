package run.wyatt.oneplatform.todo.model.form;

import lombok.Data;

import java.util.Date;

/**
 * @author Wyatt
 * @date 2023/7/17 15:43
 */
@Data
public class TodoForm {
    private Long id;
    private Long tenant;
    private String category;
    private String name;
    private String detail;
    private Boolean emergency;
    private Boolean importance;
    private String workload;
    private Double workloadHour;
    private Integer workloadDay;
    private Date deadline;
    private Date submitTime;
    private Date beginTime;
    private Date finishTime;
    private String status;
    private String conclusion;
    private Long lastLogId;
}
