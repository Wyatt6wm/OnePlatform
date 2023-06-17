package run.wyatt.oneplatform.system.model.form;

import lombok.Data;

/**
 * @author Wyatt
 * @date 2023/6/17 11:22
 */
@Data
public class SignUpForm {
    private String username;
    private String password;
    private String verifyCodeKey;
    private String verifyCode;
}
