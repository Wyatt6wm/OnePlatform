package run.wyatt.oneplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.wyatt.oneplatform.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
