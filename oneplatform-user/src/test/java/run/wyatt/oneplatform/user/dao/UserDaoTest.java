package run.wyatt.oneplatform.user.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import run.wyatt.oneplatform.user.TestBaseClass;
import run.wyatt.oneplatform.user.model.entity.User;

/**
 * @author Wyatt
 * @date 2023/6/9 16:44
 */
public class UserDaoTest extends TestBaseClass {
    @Autowired
    private UserDao userDao;

    @Test
    public void testCount() {
        long count = userDao.count();
        Assert.assertEquals(1L, count);
    }

    @Test
    public void testFindByUsername() {
        User user = userDao.findByUsername("administrator");
        Assert.assertEquals(1L, user.getId().longValue());
    }
}
