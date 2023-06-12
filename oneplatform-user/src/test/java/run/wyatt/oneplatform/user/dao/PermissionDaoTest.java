package run.wyatt.oneplatform.user.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import run.wyatt.oneplatform.user.TestBaseClass;
import run.wyatt.oneplatform.user.model.entity.Permission;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:36
 */
public class PermissionDaoTest extends TestBaseClass {
    @Autowired
    private PermissionDao permissionDao;

    @Test
    public void testFindActiatedPermissionsByUserId() {
        List<Permission> permissions = permissionDao.findActiatedPermissionsByUserId(1L);
        Assert.assertEquals(3, permissions.size());
        for (Permission permission : permissions) {
            System.out.println("" + permission.getId() + " " +permission.getName());
        }
    }
}
