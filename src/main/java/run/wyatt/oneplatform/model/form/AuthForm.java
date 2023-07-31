package run.wyatt.oneplatform.model.form;

import lombok.Data;

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
}
