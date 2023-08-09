package run.wyatt.oneplatform.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author Wyatt
 * @date 2023/7/18 12:32
 */
@Data
public class TodoLog {
    private String uuid;
    private Long tenant;
    private String todoUuid;
    private String title;
    private String log;
    private String logType;
    private Date submitTime;
}
