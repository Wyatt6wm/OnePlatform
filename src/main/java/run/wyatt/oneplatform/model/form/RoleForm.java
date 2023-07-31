package run.wyatt.oneplatform.model.form;

import lombok.Data;

/**
 * @author Wyatt
 * @date 2023/7/7 18:08
 */
@Data
public class RoleForm {
    private Long id;
    private String identifier;
    private String name;
    private String description;
    private Boolean activated;
}
