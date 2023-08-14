package run.wyatt.oneplatform.model.entity.support;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
@Data
public class RoleAuthPK implements Serializable {
    private Long roleId;
    private Long authId;
}
