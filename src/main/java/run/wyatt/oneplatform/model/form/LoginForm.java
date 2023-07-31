package run.wyatt.oneplatform.model.form;

import lombok.Data;

/**
 * 登录请求表单
 *
 * @author Wyatt
 * @date 2023/6/2 17:28
 */
@Data
public class LoginForm {
    private String username;
    private String password;
    private String captchaKey;
    private String captchaInput;
}
