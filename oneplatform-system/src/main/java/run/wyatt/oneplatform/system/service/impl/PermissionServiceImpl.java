package run.wyatt.oneplatform.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.system.dao.PermissionDao;
import run.wyatt.oneplatform.system.model.entity.Permission;
import run.wyatt.oneplatform.system.service.PermissionService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:50
 */
@Service
public class PermissionServiceImpl implements PermissionService {
    @Autowired
    private PermissionDao permissionDao;

    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        try {
            return permissionDao.findActivatedPermissionsByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("数据库错误");
        }
    }
}
