package run.wyatt.oneplatform.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.wyatt.oneplatform.model.constant.RedisConst;
import run.wyatt.oneplatform.model.entity.Role;
import run.wyatt.oneplatform.model.entity.RoleAuth;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.repository.RoleAuthRepository;
import run.wyatt.oneplatform.repository.RoleRepository;
import run.wyatt.oneplatform.repository.UserRoleRepository;
import run.wyatt.oneplatform.service.AuthService;
import run.wyatt.oneplatform.service.RoleService;

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
    //    @Autowired
//    private RoleDao roleDao;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private RoleAuthRepository roleAuthRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    //    @Autowired
//    private RoleAuthDao roleAuthDao;
//    @Autowired
//    private UserRoleDao userRoleDao;
    @Autowired
    private AuthService authService;


    @Override
    public Role createRole(Role role) {
        log.info("输入参数: {}", role);
        Assert.notNull(role, "输入参数为空");

        try {
            log.info("插入记录");
            role.setId(null);
            role = roleRepository.save(role);
            log.info("成功插入记录: id={}", role.getId());
            return role;
        } catch (DataIntegrityViolationException e) {
            log.info(e.getMessage());
            throw new BusinessException("角色标识符重复");
        }
    }

    @Override
    public void grant(Long roleId, Long authId) {
        log.info("输入参数: roleId={}, authId={}", roleId, authId);
        Assert.notNull(roleId, "roleId为null");
        Assert.notNull(authId, "authId为null");

        List<Long> authIds = new ArrayList<>();
        authIds.add(authId);
        if (!grant(roleId, authIds).isEmpty()) {
            throw new BusinessException("授权失败");
        }
    }

    @Override
    public List<Long> grant(Long roleId, List<Long> authIds) {
        log.info("输入参数: roleId={}, authIds={}", roleId, authIds);
        Assert.notNull(roleId, "roleId为null");
        Assert.notNull(authIds, "authIds为null");

        List<Long> failList = new ArrayList<>();
        for (Long authId : authIds) {
            try {
                RoleAuth roleAuth = new RoleAuth();
                roleAuth.setRoleId(roleId);
                roleAuth.setAuthId(authId);
                roleAuthRepository.save(roleAuth);
                log.info("授权成功: (roleId={}, authId={})", roleId, authId);
            } catch (Exception e) {
                failList.add(authId);
            }
        }

        if (failList.size() < authIds.size()) {
            log.info("有授权成功，要更新authDbChangeTime时间戳，以动态更新用户权限缓存");
            authService.updateAuthDbChangeTime();
        }

        log.info("授权失败的authIds：{}", failList);
        return failList;
    }

    @Override
    public List<Long> ungrant(Long roleId, List<Long> authIds) {
        log.info("输入参数: roleId={}, authIds={}", roleId, authIds);
        Assert.notNull(roleId, "roleId为null");
        Assert.notNull(authIds, "authIds为null");

        List<Long> failList = new ArrayList<>();
        for (Long authId : authIds) {
            try {
                RoleAuth roleAuth = new RoleAuth();
                roleAuth.setRoleId(roleId);
                roleAuth.setAuthId(authId);
                roleAuthRepository.delete(roleAuth);
                log.info("解除授权成功: (roleId={}, authId={})", roleId, authId);
            } catch (Exception e) {
                failList.add(authId);
            }
        }

        if (failList.size() < authIds.size()) {
            log.info("有解除授权成功，要更新authDbChangeTime时间戳，以动态更新用户权限缓存");
            authService.updateAuthDbChangeTime();
        }

        log.info("解除授权失败的authId：{}", failList);
        return failList;
    }

    @Override
    public void removeRole(Long roleId) {
        log.info("输入参数: roleId={}", roleId);
        Assert.notNull(roleId, "输入参数为空");


        log.info("删除与本角色相关的用户-角色、角色-权限关联关系");
        userRoleRepository.deleteByRoleId(roleId);
        roleAuthRepository.deleteByRoleId(roleId);
        log.info("删除本权限记录");
        roleRepository.deleteById(roleId);

        log.info("更新roleDbChangeTime时间戳");
        updateRoleDbChangeTime();
        log.info("更新authDbChangeTime时间戳");
        authService.updateAuthDbChangeTime();
    }

    @Override
    public Role updateRole(Role role) {
        log.info("输入参数: role={}", role);
        Assert.notNull(role, "输入参数为空");
        Assert.notNull(role.getId(), "id为null");

        try {
            log.info("更新记录");
            role = roleRepository.save(role);
            log.info("更新roleDbChangeTime时间戳");
            updateRoleDbChangeTime();
            log.info("更新authDbChangeTime时间戳");
            authService.updateAuthDbChangeTime();
            return role;
        } catch (DataIntegrityViolationException e) {
            log.info(e.getMessage());
            throw new BusinessException("角色标识符重复");
        }
    }

    @Override
    public void updateRoleDbChangeTime() {
        Date now = new Date();
        redis.opsForValue().set(RedisConst.ROLE_DB_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新roleDbChanged缓存为: {}", sdf.format(now));
    }

    @Override
    public void updateRoleRedisChangeTime() {
        Date now = new Date();
        StpUtil.getSession().set(RedisConst.ROLE_REDIS_CHANGE_TIME, now);
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
            userSession.set(RedisConst.REFRESH_ROLE_REDIS, flag);
            log.info("已设置refreshRoleRedis为: {}", flag);
            return;
        }
        log.info("用户{}未登录，无法设置refreshRoleRedis", userId);
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
            Integer refreshRoleRedis = (Integer) userSession.get(RedisConst.REFRESH_ROLE_REDIS);
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

        Date t1 = (Date) redis.opsForValue().get(RedisConst.ROLE_DB_CHANGE_TIME);
        Date t2 = (Date) StpUtil.getSession().get(RedisConst.ROLE_REDIS_CHANGE_TIME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("数据库更新时间: {}", t1 == null ? null : sdf.format(t1));
        log.info("缓存更新时间: {}", t2 == null ? null : sdf.format(t2));

        List<String> roles = null;
        if ((t1 == null || (t2 != null && t1.compareTo(t2) <= 0)) && noNeedRefreshRoleRedis(userId)) {
            log.info("先查询Redis的Session缓存");
            roles = (List<String>) StpUtil.getSession().get(RedisConst.ROLES);
        }

        if (roles == null) {
            log.info("无缓存或缓存不是最新，查询数据库，并更新缓存");
            List<Role> activatedRoles = roleRepository.findActivatedByUserId(userId);
            log.info("提取标识符字符串列表");
            roles = new ArrayList<>();
            for (Role item : activatedRoles) {
                roles.add(item.getIdentifier());
            }
            log.info("更新缓存");
            StpUtil.getSession().set(RedisConst.ROLES, roles);
            log.info("更新roleRedisChangeTime时间戳，更新refreshRoleRedis标记为0");
            updateRoleRedisChangeTime();
            setRefreshRoleRedisFalse(userId);
        }

        log.info("用户的角色标识列表: {}", roles);
        return roles;
    }

    @Override
    public List<Role> listRoles() {
        log.info("查询全部角色");
        List<Role> roleList = roleRepository.findAll();
        log.info("查询结果: {}", roleList);
        return roleList;
    }

    @Override
    public List<Role> listRoles(Long userId) {
        log.info("输入参数: userId={}", userId);
        List<Role> roles = roleRepository.findByUserId(userId);
        log.info("用户 {} 的角色列表: {}", userId, roles);
        return roles;
    }
}
