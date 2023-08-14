package run.wyatt.oneplatform.model.entity.support;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
@Data
public class UserRolePK implements Serializable {
    private Long userId;
    private Long roleId;
}
