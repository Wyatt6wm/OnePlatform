package run.wyatt.oneplatform.user.model.entity;

import lombok.Data;

/**
 * @author Wyatt
 * @date 2023/6/9 10:42
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String salt;
    private String nickname;
    private String motto;
    private String avatar;
}
