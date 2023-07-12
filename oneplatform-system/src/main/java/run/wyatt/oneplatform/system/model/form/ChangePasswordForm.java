package run.wyatt.oneplatform.system.model.form;

import lombok.Data;

/**
 * @author Wyatt
 * @date 2023/7/12 16:53
 */
@Data
public class ChangePasswordForm {
    private Long userId;
    private String password;
    private String newPassword;
    private String captchaKey;
    private String captchaInput;
}
