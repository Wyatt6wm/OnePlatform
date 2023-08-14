package run.wyatt.oneplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import run.wyatt.oneplatform.model.entity.Role;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("select distinct r.id, r.identifier, r.name, r.description, r.activated " +
            " from UserRole ur " +
            "      inner join Role r on ur.roleId = r.id " +
            " where ur.userId = ?1")
    List<Role> findByUserId(Long userId);

    @Query("select distinct r.id, r.identifier, r.name, r.description, r.activated " +
            " from UserRole ur " +
            "      inner join Role r on ur.roleId = r.id and r.activated = 1 " +
            " where ur.userId = ?1")
    List<Role> findActivatedByUserId(Long userId);
}
