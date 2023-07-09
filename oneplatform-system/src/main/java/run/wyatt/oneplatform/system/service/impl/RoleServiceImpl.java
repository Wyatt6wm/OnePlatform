package run.wyatt.oneplatform.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.dao.RoleDao;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.entity.Role;
import run.wyatt.oneplatform.system.model.form.RoleAuthForm;
import run.wyatt.oneplatform.system.service.RoleService;

import java.util.Date;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/7/7 17:41
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RedisTemplate<String, Object> redis;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private AuthDao authDao;

    @Override
    public Role createRole(Role role) {
        log.info("输入参数: role={}", role);

        long rows = 0;
        try {
            role.setId(null);
            rows = roleDao.insert(role);
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage());
            throw new BusinessException("角色标识符重复");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        if (rows == 0) {

        }

        updateRoleDbChanged();

        log.info("成功创建角色: roleId={}", role.getId());
        return role;
    }

    @Override
    public void changeRoleGrants(List<RoleAuthForm> grant, List<RoleAuthForm> disgrant) {
        log.info("输入参数: grant={}, disgrant={}", grant, disgrant);

        for (RoleAuthForm item: grant) {

        }
//        updateRoleDbChanged();
    }

    @Override
    public void removeRole(Long roleId) {
        log.info("输入参数: roleId={}", roleId);

        long rows = 0;
        try {
            rows = roleDao.delete(roleId);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        if (rows == 0) {
            throw new BusinessException("该角色数据不存在");
        }

        updateRoleDbChanged();

        log.info("成功删除角色记录");
    }

    @Override
    public Role updateRole(Long roleId, Role role) {
        log.info("输入参数: roleId={}, role={}", roleId, role);

        if (roleId == null) {
            throw new BusinessException("角色ID错误");
        }

        long rows = 0;
        try {
            rows = roleDao.update(roleId, role);
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage());
            throw new BusinessException("角色标识符重复");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        if (rows == 0) {
            throw new BusinessException("该角色数据不存在");
        }

        updateRoleDbChanged();

        log.info("成功更新角色记录");
        role.setId(roleId);
        return role;
    }

    @Override
    public void updateRoleDbChanged() {
        Date now = new Date();
        redis.opsForValue().set(SysConst.ROLE_DB_CHANGED, now);
        log.info("已更新roleDbChanged缓存为: {}", now);
    }

    @Override
    public List<Role> listAllRoles() {
        try {
            List<Role> roleList = roleDao.findAll();
            log.info("成功查询全部角色: {}", roleList);
            return roleList;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
    }

    @Override
    public List<Auth> listRoleAuths(Long roleId) {
        log.info("输入参数: roleId={}", roleId);
        try {
            List<Auth> authList = authDao.findByRoleId(roleId);
            log.info("成功查询用户的全部权限: {}", authList);
            return authList;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
    }
}
