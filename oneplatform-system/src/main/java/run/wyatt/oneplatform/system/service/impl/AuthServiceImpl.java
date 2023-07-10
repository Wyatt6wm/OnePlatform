package run.wyatt.oneplatform.system.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.service.AuthService;

import java.text.SimpleDateFormat;
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

        updateAuthDbChanged();

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

        updateAuthDbChanged();

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

        updateAuthDbChanged();

        log.info("成功更新权限记录");
        auth.setId(authId);
        return auth;
    }

    @Override
    public void updateAuthDbChanged() {
        Date now = new Date();
        redis.opsForValue().set(SysConst.AUTH_DB_CHANGED, now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd HH:mm:ss.SSS");
        log.info("已更新authDbChanged缓存为: {}", sdf.format(now));
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
}
