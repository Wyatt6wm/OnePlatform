package run.wyatt.oneplatform.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Wyatt
 * @date 2023/7/17 15:21
 */
@Data
public class Todo {
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
}
