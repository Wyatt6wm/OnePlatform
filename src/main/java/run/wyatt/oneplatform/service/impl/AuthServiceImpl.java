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
import run.wyatt.oneplatform.model.constant.RoleConst;
import run.wyatt.oneplatform.model.entity.Auth;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.repository.AuthRepository;
import run.wyatt.oneplatform.repository.RoleAuthRepository;
import run.wyatt.oneplatform.service.AuthService;
import run.wyatt.oneplatform.service.RoleService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:50
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private RedisTemplate<String, Object> redis;
    @Autowired
    private AuthRepository authRepository;
    @Autowired
    private RoleAuthRepository roleAuthRepository;
    @Autowired
    private RoleService roleService;

    @Override
    public Auth createAuth(Auth auth) {
        log.info("输入参数: {}", auth);
        Assert.notNull(auth, "输入参数为空");

        try {
            log.info("插入记录");
            auth.setId(null);
            auth = authRepository.save(auth);
            log.info("成功插入记录: id={}", auth.getId());
        } catch (DataIntegrityViolationException e) {
            log.info(e.getMessage());
            throw new BusinessException("权限标识符重复");
        }

        try {
            log.info("自动授权给超级管理员");
            roleService.grant(RoleConst.SUPER_ADMIN_ID, auth.getId());
        } catch (BusinessException e) {
            log.info("自动授权给超级管理员失败，须手动授权");
        }

        return auth;
    }

    @Override
    public void removeAuth(Long id) {
        log.info("输入参数: id={}", id);
        Assert.notNull(id, "输入参数为空");

        log.info("删除与本权限相关的角色-权限关联关系");
        roleAuthRepository.deleteByAuthId(id);
        log.info("删除本权限记录");
        authRepository.deleteById(id);

        log.info("更新“权限数据库变更时间”时间戳");
        updateAuthDbChangeTime();
    }

    @Override
    public Auth updateAuth(Auth auth) {
        log.info("输入参数: {}", auth);
        Assert.notNull(auth, "输入参数为空");
        Assert.notNull(auth.getId(), "id为null");

        try {
            log.info("更新记录");
            authRepository.save(auth);
            log.info("更新“权限数据库变更时间”时间戳");
            updateAuthDbChangeTime();
            return auth;
        } catch (DataIntegrityViolationException e) {
            log.info(e.getMessage());
            throw new BusinessException("权限标识符重复");
        }
    }

    @Override
    public void updateAuthDbChangeTime() {
        Date now = new Date();
        redis.opsForValue().set(RedisConst.AUTH_DB_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新authDbChanged缓存为: {}", sdf.format(now));
    }

    @Override
    public void updateAuthRedisChangeTime() {
        Date now = new Date();
        StpUtil.getSession().set(RedisConst.AUTH_REDIS_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新authRedisChangeTime缓存为: {}", sdf.format(now));
    }

    /**
     * 若用户已登录，将Session的refreshAuthRedis设置为0/1
     * 表示用户查询其缓存的权限标识时应该访问数据库来更新缓存
     * 1-需要查询数据库更新缓存，0-不需要
     *
     * @param userId 用户ID
     * @param flag   0/1
     */
    private void setRefreshAuthRedis(Long userId, int flag) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            userSession.set(RedisConst.REFRESH_AUTH_REDIS, flag);
            log.info("已设置refreshAuthRedis为: {}", flag);
            return;
        }
        log.info("用户{}未登录，无法设置refreshAuthRedis", userId);
    }

    @Override
    public void setRefreshAuthRedisTrue(Long userId) {
        setRefreshAuthRedis(userId, 1);
    }

    @Override
    public void setRefreshAuthRedisFalse(Long userId) {
        setRefreshAuthRedis(userId, 0);
    }

    /**
     * 判断不需要更新缓存的auth标识
     *
     * @param userId 用户ID
     * @return true: 不需要更新 / false: 需要更新
     */
    private boolean noNeedRefreshAuthRedis(Long userId) {
        SaSession userSession = StpUtil.getSessionByLoginId(userId);
        if (userSession != null) {
            Integer refreshAuthRedis = (Integer) userSession.get(RedisConst.REFRESH_AUTH_REDIS);
            return refreshAuthRedis == null || refreshAuthRedis == 0;
        }
        return true;
    }

    // 规则参考getRoleIdentifiers()前写的规则
    @Override
    public List<String> getActivatedAuthIdentifiers(Long userId) {
        log.info("输入参数: userId={}", userId);
        Assert.notNull(userId, "userId为null");

        Date t1 = (Date) redis.opsForValue().get(RedisConst.AUTH_DB_CHANGE_TIME);
        Date t2 = (Date) StpUtil.getSession().get(RedisConst.AUTH_REDIS_CHANGE_TIME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("数据库更新时间: {}", t1 == null ? null : sdf.format(t1));
        log.info("缓存更新时间: {}", t2 == null ? null : sdf.format(t2));

        List<String> auths = null;
        if ((t1 == null || (t2 != null && t1.compareTo(t2) <= 0)) && noNeedRefreshAuthRedis(userId)) {
            log.info("先查询Redis的Session缓存");
            auths = (List<String>) StpUtil.getSession().get(RedisConst.AUTHS);
        }

        if (auths == null) {
            log.info("无缓存或缓存不是最新，查询数据库，并更新缓存");
            List<Auth> activatedAuths = authRepository.findActivatedByUserId(userId);
            log.info("提取标识符字符串列表");
            auths = new ArrayList<>();
            for (Auth item : activatedAuths) {
                auths.add(item.getIdentifier());
            }
            log.info("更新缓存");
            StpUtil.getSession().set(RedisConst.AUTHS, auths);
            log.info("更新authRedisChangeTime时间戳，更新refreshAuthRedis标记为0");
            updateAuthRedisChangeTime();
            setRefreshAuthRedisFalse(userId);
        }

        log.info("用户的权限标识列表: {}", auths);
        return auths;
    }

    @Override
    public List<Auth> listAuths() {
        log.info("查询全部权限");
        List<Auth> authList = authRepository.findAll();
        log.info("查询结果: {}", authList);
        return authList;
    }

    @Override
    public List<Auth> listAuths(Long roleId) {
        log.info("输入参数: roleId={}", roleId);
        Assert.notNull(roleId, "roleId为null");

        List<Auth> auths = authRepository.findByRoleId(roleId);
        log.info("查询结果: {}", auths);
        return auths;
    }
}
