package run.wyatt.oneplatform.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.utils.PasswordUtils;
import run.wyatt.oneplatform.user.dao.UserDao;
import run.wyatt.oneplatform.user.model.entity.User;
import run.wyatt.oneplatform.user.service.UserService;

/**
 * @author Wyatt
 * @date 2023/6/9 10:38
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public User verifyUserByUsername(String username, String password) {
        User user = null;
        
        try {
            user = userDao.findByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("数据库错误");
        }

        if (user == null || !user.getPassword().equals(PasswordUtils.encode(password, user.getSalt()))) {
            throw new RuntimeException("用户名或密码错误");
        }

        // TODO 检查账号状态

        return user;
    }
}
