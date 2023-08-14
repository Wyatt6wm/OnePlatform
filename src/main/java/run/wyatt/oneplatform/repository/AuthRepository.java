package run.wyatt.oneplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import run.wyatt.oneplatform.model.entity.Auth;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/8/11
 */
public interface AuthRepository extends JpaRepository<Auth, Long> {
    @Query("select a.id, a.identifier, a.name, a.description, a.activated " +
            " from RoleAuth ra " +
            "      inner join Auth a on ra.authId = a.id " +
            " where ra.roleId = ?1")
    List<Auth> findByRoleId(Long roleId);

    @Query("select distinct a.id, a.identifier, a.name, a.description, a.activated " +
            " from UserRole ur " +
            "      inner join Role r on ur.roleId = r.id and r.activated = 1 " +
            "      inner join RoleAuth ra on ur.roleId = ra.roleId " +
            "      inner join Auth a on ra.authId = a.id and a.activated = 1 " +
            " where ur.userId = ?1")
    List<Auth> findActivatedByUserId(Long userId);
}
