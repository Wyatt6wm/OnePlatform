package run.wyatt.oneplatform.system.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.cosnt.CommonConst;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.dao.RoleAuthDao;
import run.wyatt.oneplatform.system.dao.RoleDao;
import run.wyatt.oneplatform.system.model.constant.SysConst;
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
            rows = roleDao.insert(role);
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage());
            throw new BusinessException("角色标识符重复");
        }

        if (rows == 0) {
            throw new BusinessException("创建角色失败");
        }

        updateRoleDbChangeTime();
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
                roleAuthDao.insert(roleId, authId);
                log.info("授权成功: (roleId={}, authId={})", roleId, authId);
            } catch (Exception e) {
                failList.add(authId);
            }
        }
        log.info("授权失败的authId：{}", failList);

        // 有授权成功时，要更新标志，以动态更新用户权限缓存
        if (failList.size() < authIds.size()) {
            authService.updateAuthDbChangeTime();
        }

        return failList;
    }

    @Override
    public List<Long> ungrant(Long roleId, List<Long> authIds) {
        log.info("输入参数: roleId={}, authIds={}", roleId, authIds);
        if (roleId == null || authIds == null) throw new BusinessException("参数错误");

        List<Long> failList = new ArrayList<>();
        for (Long authId : authIds) {
            try {
                roleAuthDao.delete(roleId, authId);
                log.info("解除授权成功: (roleId={}, authId={})", roleId, authId);
            } catch (Exception e) {
                failList.add(authId);
            }
        }
        log.info("解除授权失败的authId：{}", failList);

        // 有解除授权成功时，要更新标志，以动态更新用户权限缓存
        if (failList.size() < authIds.size()) {
            authService.updateAuthDbChangeTime();
        }

        return failList;
    }

    @Override
    public void removeRole(Long roleId) {
        log.info("输入参数: roleId={}", roleId);

        if (roleDao.delete(roleId) == 0) {
            throw new BusinessException("该角色数据不存在");
        }

        updateRoleDbChangeTime();
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
        }

        if (rows == 0) {
            throw new BusinessException("该角色数据不存在");
        }

        updateRoleDbChangeTime();
        log.info("成功更新角色记录");
        role.setId(roleId);
        return role;
    }

    @Override
    public void updateRoleDbChangeTime() {
        Date now = new Date();
        redis.opsForValue().set(SysConst.ROLE_DB_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新roleDbChanged缓存为: {}", sdf.format(now));
    }

    private void updateRoleRedisChangeTime() {
        Date now = new Date();
        StpUtil.getSession().set(SysConst.ROLE_REDIS_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新roleRedisChangeTime缓存为: {}", sdf.format(now));
    }

    /**
     * 若用户已登录，将Session的refreshRoleRedis设置为0/1
     * 表示用户查询其缓存的权限标识时是否应该访问数据库来更新缓存
     * 1-需要查询数据库更新缓存，0-不需要
     *
     * @param userId 用户ID
     */
    private void setRefreshRoleRedis(Long userId, int flag) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            userSession.set(SysConst.REFRESH_ROLE_REDIS, flag);
            log.info("已设置refreshRoleRedis为: {}", flag);
            return;
        }
        log.info("用户 {} 未登录，无法设置refreshRoleRedis", userId);
    }

    @Override
    public void setRefreshRoleRedisTrue(Long userId) {
        setRefreshRoleRedis(userId, 1);
    }

    @Override
    public void setRefreshRoleRedisFalse(Long userId) {
        setRefreshRoleRedis(userId, 0);
    }

    /**
     * 判断不需要更新缓存的role标识
     *
     * @param userId 用户ID
     * @return true: 不需要更新 / false: 需要更新
     */
    private boolean noNeedRefreshRoleRedis(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            Integer refreshRoleRedis = (Integer) userSession.get(SysConst.REFRESH_ROLE_REDIS);
            return refreshRoleRedis == null || refreshRoleRedis == 0;
        }
        return true;
    }

    // 先查询缓存的情况：数据库无变更（对比时间标记） and 绑定关系无变更（refreshRoleRedis/refreshAuthRedis是否值为1）
    // 一、判断数据库无变更
    //      1、无dbChanged ==> 数据库无变更
    //      2、有dbChanged、有redisChanged，且dbChanged <= redisChanged ==> 数据库有变更，但是已经更新过缓存了
    // 二、判断绑定关系无变更
    //      缓存中refreshRoleRedis/refreshAuthRedis不存在，或者值为0
    @Override
    public List<String> getActivatedRoleIdentifiers(Long userId) {
        log.info("输入参数: userId={}", userId);

        Date t1 = (Date) redis.opsForValue().get(SysConst.ROLE_DB_CHANGE_TIME);
        Date t2 = (Date) StpUtil.getSession().get(SysConst.ROLE_REDIS_CHANGE_TIME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("数据库更新时间： {}", t1 == null ? null : sdf.format(t1));
        log.info("缓存更新时间: {}", t2 == null ? null : sdf.format(t2));

        List<String> roles = null;
        if ((t1 == null || (t2 != null && t1.compareTo(t2) <= 0)) && noNeedRefreshRoleRedis(userId)) {
            log.info("先查询Redis的Session缓存");
            roles = (List<String>) StpUtil.getSession().get(CommonConst.REDIS_ROLES_KEY);
        }

        if (roles == null) {
            log.info("无缓存或缓存不是最新，查询数据库，并更新缓存");
            List<Role> activatedRoles = roleDao.findActivatedByUserId(userId);
            roles = new ArrayList<>();
            for (Role item : activatedRoles) {
                roles.add(item.getIdentifier());
            }
            log.info("完成标识符字符串列表提取");
            StpUtil.getSession().set(CommonConst.REDIS_ROLES_KEY, roles);
            log.info("已更新缓存");
            updateRoleRedisChangeTime();
            setRefreshRoleRedisFalse(userId);
        }

        log.info("用户的角色标识列表: {}", roles);
        return roles;
    }

    @Override
    public List<Role> listRoles() {
        List<Role> roles = roleDao.findAll();
        log.info("成功查询全部角色: {}", roles);
        return roles;
    }

    @Override
    public List<Role> listRoles(Long userId) {
        log.info("输入参数: userId={}", userId);
        List<Role> roles = roleDao.findByUserId(userId);
        log.info("用户 {} 的角色列表: {}", userId, roles);
        return roles;
    }
}
