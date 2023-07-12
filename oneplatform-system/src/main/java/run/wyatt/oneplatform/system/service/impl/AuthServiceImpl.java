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
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.entity.Role;
import run.wyatt.oneplatform.system.service.AuthService;

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
    private AuthDao authDao;

    @Override
    public Auth createAuth(Auth auth) {
        log.info("输入参数: auth={}", auth);

        long rows = 0;
        try {
            auth.setId(null);
            rows = authDao.insert(auth);
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage());
            throw new BusinessException("权限标识符重复");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        if (rows == 0) {
            throw new BusinessException("创建权限失败");
        }

        updateAuthDbChangeTime();

        log.info("成功创建权限: authId={}", auth.getId());
        return auth;
    }

    @Override
    public void removeAuth(Long authId) {
        log.info("输入参数: authId={}", authId);

        long rows = 0;
        try {
            rows = authDao.delete(authId);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        if (rows == 0) {
            throw new BusinessException("该权限数据不存在");
        }

        updateAuthDbChangeTime();

        log.info("成功删除权限记录");
    }

    @Override
    public Auth updateAuth(Long authId, Auth auth) {
        log.info("输入参数: authId={}, auth={}", authId, auth);

        if (authId == null) {
            throw new BusinessException("权限ID错误");
        }

        long rows = 0;
        try {
            rows = authDao.update(authId, auth);
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage());
            throw new BusinessException("权限标识符重复");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }

        if (rows == 0) {
            throw new BusinessException("该权限数据不存在");
        }

        updateAuthDbChangeTime();

        log.info("成功更新权限记录");
        auth.setId(authId);
        return auth;
    }

    @Override
    public void updateAuthDbChangeTime() {
        Date now = new Date();
        redis.opsForValue().set(SysConst.AUTH_DB_CHANGE_TIME, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新authDbChanged缓存为: {}", sdf.format(now));
    }

    private void updateAuthRedisChangeTime() {
        Date now = new Date();
        StpUtil.getSession().set(SysConst.AUTH_REDIS_CHANGE_TIME, now);
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
            userSession.set(SysConst.REFRESH_AUTH_REDIS, flag);
            log.info("已设置refreshAuthRedis为: {}", flag);
            return;
        }
        log.info("用户 {} 未登录，无法设置refreshAuthRedis", userId);
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
            return 0 == (int) userSession.get(SysConst.REFRESH_AUTH_REDIS);
        }
        return true;
    }

    // 规则参考getRoleIdentifiers()前写的规则
    @Override
    public List<String> getActivatedAuthIdentifiers(Long userId) {
        log.info("输入参数: userId={}", userId);

        Date t1 = (Date) redis.opsForValue().get(SysConst.AUTH_DB_CHANGE_TIME);
        Date t2 = (Date) StpUtil.getSession().get(SysConst.AUTH_REDIS_CHANGE_TIME);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("数据库更新时间： {}", t1 == null ? null : sdf.format(t1));
        log.info("缓存更新时间: {}", t2 == null ? null : sdf.format(t2));

        List<String> auths = null;
        if ((t1 == null || (t2 != null && t1.compareTo(t2) <= 0)) && noNeedRefreshAuthRedis(userId)) {
            log.info("先查询Redis的Session缓存");
            auths = (List<String>) StpUtil.getSession().get(CommonConst.REDIS_AUTHS_KEY);
        }

        if (auths == null) {
            log.info("无缓存或缓存不是最新，查询数据库，并更新缓存");
            List<Auth> activatedAuths = authDao.findActivatedByUserId(userId);
            auths = new ArrayList<>();
            for (Auth item : activatedAuths) {
                auths.add(item.getIdentifier());
            }
            log.info("完成标识符字符串列表提取");
            StpUtil.getSession().set(CommonConst.REDIS_AUTHS_KEY, auths);
            log.info("已更新缓存");
            updateAuthRedisChangeTime();
            setRefreshAuthRedisTrue(userId);
        }

        log.info("用户的权限标识列表: {}", auths);
        return auths;
    }

    @Override
    public List<Auth> listAllAuths() {
        try {
            List<Auth> authList = authDao.findAll();
            log.info("成功查询全部权限: {}", authList);
            return authList;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
    }

    @Override
    public List<Auth> listAuths(Long roleId) {
        log.info("输入参数: roleId={}", roleId);
        List<Auth> auths = authDao.findByRoleId(roleId);
        log.info("角色 {} 的权限列表: {}", roleId, auths);
        return auths;
    }
}
