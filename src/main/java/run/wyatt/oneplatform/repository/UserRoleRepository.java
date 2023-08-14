package run.wyatt.oneplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.wyatt.oneplatform.model.entity.UserRole;
import run.wyatt.oneplatform.model.entity.support.UserRolePK;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
public interface UserRoleRepository extends JpaRepository<UserRole, UserRolePK> {
}
