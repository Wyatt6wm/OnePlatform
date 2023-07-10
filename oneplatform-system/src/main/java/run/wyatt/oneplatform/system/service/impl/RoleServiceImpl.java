package run.wyatt.oneplatform.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.dao.RoleAuthDao;
import run.wyatt.oneplatform.system.dao.RoleDao;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.entity.Role;
import run.wyatt.oneplatform.system.service.AuthService;
import run.wyatt.oneplatform.system.service.RoleService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    @Autowired
    private RoleAuthDao roleAuthDao;
    @Autowired
    private AuthService authService;


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
            throw new BusinessException("创建角色失败");
        }

        updateRoleDbChanged();

        log.info("成功创建角色: roleId={}", role.getId());
        return role;
    }

    @Override
    public List<Long> grant(Long roleId, List<Long> authIds) {
        log.info("输入参数: roleId={}, authIds={}", roleId, authIds);
        if (roleId == null || authIds == null) throw new BusinessException("参数错误");

        List<Long> failList = new ArrayList<>();
        for (Long authId : authIds) {
            try {
                long rows = roleAuthDao.insert(roleId, authId);
                log.info("授权成功: (roleId={}, authId={})", roleId, authId);
            } catch (Exception e) {
                failList.add(authId);
            }
        }
        log.info("授权失败的authId：{}", failList);

        // 有授权成功时，要更新标志，以动态更新用户权限缓存
        if (failList.size() < authIds.size()) {
            authService.updateAuthDbChanged();
        }

        return failList;
    }

    @Override
    public List<Long> disgrant(Long roleId, List<Long> authIds) {
        log.info("输入参数: roleId={}, authIds={}", roleId, authIds);
        if (roleId == null || authIds == null) throw new BusinessException("参数错误");

        List<Long> failList = new ArrayList<>();
        for (Long authId : authIds) {
            try {
                long rows = roleAuthDao.delete(roleId, authId);
                log.info("解除授权成功: (roleId={}, authId={})", roleId, authId);
            } catch (Exception e) {
                failList.add(authId);
            }
        }
        log.info("解除授权失败的authId：{}", failList);

        // 有授权成功时，要更新标志，以动态更新用户权限缓存
        if (failList.size() < authIds.size()) {
            authService.updateAuthDbChanged();
        }

        return failList;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新roleDbChanged缓存为: {}", sdf.format(now));
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
