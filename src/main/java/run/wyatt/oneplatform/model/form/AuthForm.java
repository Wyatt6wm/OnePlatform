package run.wyatt.oneplatform.model.form;

import lombok.Data;
import run.wyatt.oneplatform.model.entity.Auth;

/**
 * @author Wyatt
 * @date 2023/7/2 19:37
 */
@Data
public class AuthForm {
    private Long id;
    private String identifier;
    private String name;
    private String description;
    private Boolean activated;

    public Auth convert() {
        Auth auth = new Auth();

        auth.setId(id);
        if (identifier != null) auth.setIdentifier(identifier.trim());
        if (name != null) auth.setName(name.trim());
        if (description != null) auth.setDescription(description.trim());
        auth.setActivated(activated);

        return auth;
    }
}
