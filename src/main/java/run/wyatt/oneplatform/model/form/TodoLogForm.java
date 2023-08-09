package run.wyatt.oneplatform.model.form;

import lombok.Data;

import java.util.Date;

/**
 * @author Wyatt
 * @date 2023/7/20 10:26
 */
@Data
public class TodoLogForm {
    private String uuid;
    private Long tenant;
    private String todoUuid;
    private String title;
    private String log;
    private String logType;
    private Date submitTime;
}
