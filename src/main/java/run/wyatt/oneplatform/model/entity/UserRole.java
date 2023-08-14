package run.wyatt.oneplatform.model.entity;

import lombok.Data;
import run.wyatt.oneplatform.model.entity.support.UserRolePK;

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
@Table(name = "tb_um_user_role")
@IdClass(UserRolePK.class)
public class UserRole {
    @Id
    private Long userId;
    @Id
    private Long roleId;
}
