package run.wyatt.oneplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.wyatt.oneplatform.model.entity.RoleAuth;
import run.wyatt.oneplatform.model.entity.support.RoleAuthPK;

/**
 * @author Wyatt
 * @date 2023/8/14
 */
public interface RoleAuthRepository extends JpaRepository<RoleAuth, RoleAuthPK> {
    void deleteByAuthId(Long authId);
}
