package run.wyatt.oneplatform.system.model.entity;

import lombok.Data;

/**
 * @author Wyatt
 * @date 2023/6/13 11:21
 */
@Data
public class Role {
    private Long id;
    private String identifier;
    private String name;
    private String description;
    private Boolean activated;
    private Integer rank;
}
