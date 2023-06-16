package run.wyatt.oneplatform.system.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import run.wyatt.oneplatform.system.TestBaseClass;
import run.wyatt.oneplatform.system.model.entity.Role;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/13 12:48
 */
public class RoleDaoTest extends TestBaseClass {
    @Autowired
    private RoleDao roleDao;

    @Test
    public void testFindActivatedRolesByUserId() {
        List<Role> roles = roleDao.findActivatedRolesByUserId(1L);
        Assert.assertEquals(1, roles.size());
    }
}
