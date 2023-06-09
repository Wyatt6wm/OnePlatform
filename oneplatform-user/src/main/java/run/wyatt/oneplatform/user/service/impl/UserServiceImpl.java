package run.wyatt.oneplatform.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.user.dao.UserDao;
import run.wyatt.oneplatform.user.service.UserService;

/**
 * @author Wyatt
 * @date 2023/6/9 10:38
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
}
