package run.wyatt.oneplatform.system.model.form;

import lombok.Data;

/**
 * @author Wyatt
 * @date 2023/6/17 11:22
 */
@Data
public class RegistryForm {
    private String username;
    private String password;
    private String captchaKey;
    private String captchaInput;
}
