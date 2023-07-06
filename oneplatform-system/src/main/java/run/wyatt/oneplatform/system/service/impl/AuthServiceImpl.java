package run.wyatt.oneplatform.system.service.impl;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.service.AuthService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/12 15:50
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthDao authDao;

    @Override
    public Long createAuth(Auth auth) {
        log.info("输入参数: auth={}", auth);

        try {
            Long authId = authDao.insert(auth);
            log.info("数据库生成主键: authId={}", authId);
            return authId;
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage());
            throw new BusinessException("权限标识符重复");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
    }

    @Override
    public List<Auth> listAuthDetails(Long userId) {
        log.info("输入参数: userId={}", userId);

        try {
            List<Auth> authDetails = authDao.findAuthsByUserId(userId);
            log.info("成功查询权限详细信息列表: {}", authDetails);

            return authDetails;
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
    }

    @Override
    public boolean updateAuthDetail(Auth newAuth) {
        log.info("输入参数: newAuth={}", newAuth);

        if (newAuth.getId() == null) {
            throw new BusinessException("权限ID错误");
        }
        try {
            int affected = authDao.update(newAuth);
            log.info("成功更新权限记录");

            return affected == 1;
        } catch (DuplicateKeyException e) {
            log.info(e.getMessage());
            throw new BusinessException("权限标识符重复");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new DatabaseException();
        }
    }
}
