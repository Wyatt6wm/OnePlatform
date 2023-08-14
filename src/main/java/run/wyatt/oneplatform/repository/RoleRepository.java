package run.wyatt.oneplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.wyatt.oneplatform.model.entity.Role;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
}
