package run.wyatt.oneplatform.model.form;

import lombok.Data;
import run.wyatt.oneplatform.model.entity.Role;

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

    public Role convert() {
        Role role = new Role();

        role.setId(id);
        if (identifier != null ) role.setIdentifier(identifier.trim());
        if (name != null) role.setName(name.trim());
        if (description != null) role.setDescription(description.trim());
        role.setActivated(activated);

        return role;
    }
}
