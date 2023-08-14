package run.wyatt.oneplatform.model.entity;

import lombok.Data;
import run.wyatt.oneplatform.model.entity.support.RoleAuthPK;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
@Data
@Entity
@Table(name = "tb_um_role_auth")
@IdClass(RoleAuthPK.class)
public class RoleAuth {
    @Id
    private Long roleId;
    @Id
    private Long authId;
}
