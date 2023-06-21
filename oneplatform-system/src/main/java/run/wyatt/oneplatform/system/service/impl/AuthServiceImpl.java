package run.wyatt.oneplatform.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.service.AuthService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:50
 */
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthDao authDao;

    @Override
    public List<Auth> getPermissionsByUserId(Long userId) {
        try {
            return authDao.findActivatedPermissionsByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("数据库错误");
        }
    }
}
