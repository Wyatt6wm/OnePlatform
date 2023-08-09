package run.wyatt.oneplatform.model.form;

import lombok.Data;
import run.wyatt.oneplatform.model.constant.TodoConst;
import run.wyatt.oneplatform.model.entity.Todo;

import java.util.Date;

/**
 * @author Wyatt
 * @date 2023/7/17 15:43
 */
@Data
public class TodoForm {
    private String uuid;
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
    private String lastLogUuid;

    public Todo convert() {
        Todo todo = new Todo();

        if (this.uuid != null) todo.setUuid(this.uuid.trim());
        todo.setTenant(this.tenant);
        todo.setCategory(this.category);
        if (this.name != null) todo.setName(this.name.trim());
        if (this.detail != null) todo.setDetail(this.detail.trim());
        todo.setEmergency(this.emergency);
        todo.setImportance(this.importance);
        if (this.workload != null) todo.setWorkload(this.workload.trim());
        todo.setWorkloadHour(this.workloadHour);
        todo.setWorkloadDay(this.workloadDay);
        todo.setDeadline(this.deadline);
        todo.setSubmitTime(this.submitTime);
        todo.setBeginTime(this.beginTime);
        todo.setFinishTime(this.finishTime);
        todo.setStatus(this.status);
        if (this.conclusion != null) todo.setConclusion(this.conclusion.trim().trim());
        if (lastLogUuid != null) todo.setLastLogUuid(this.lastLogUuid.trim());

        return todo;
    }
}
