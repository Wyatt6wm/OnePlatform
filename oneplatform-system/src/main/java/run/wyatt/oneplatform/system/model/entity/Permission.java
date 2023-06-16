package run.wyatt.oneplatform.system.model.entity;

import lombok.Data;

/**
 * @author Wyatt
 * @date 2023/6/12 14:49
 */
@Data
public class Permission {
    private Long id;
    private String identifier;
    private String name;
    private String description;
    private Boolean activated;
}
