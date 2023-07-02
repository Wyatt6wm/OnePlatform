package run.wyatt.oneplatform.system.service.impl;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.exception.DatabaseException;
import run.wyatt.oneplatform.common.util.LogUtil;
import run.wyatt.oneplatform.common.util.PasswordUtil;
import run.wyatt.oneplatform.system.dao.AuthDao;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.entity.User;
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
    public List<Auth> listAuthDetails(Long userId) {
        LogUtil logUtil = new LogUtil("listAuthDetails");
        log.info(logUtil.serviceBeginDivider("获取用户所有权限的详细信息"));
        log.info("输入参数: userId[{}]", userId);

        try {
            List<Auth> authDetails = authDao.findAuthsByUserId(userId);
            log.info("成功查询权限详细信息列表: [{}]", JSONObject.toJSONString(authDetails));

            log.info(logUtil.serviceSuccessDivider());
            return authDetails;
        } catch (Exception e) {
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new DatabaseException();
        }
    }

    @Override
    public boolean updateAuthDetail(Auth newAuth) {
        LogUtil logUtil = new LogUtil("updateAuthDetail");
        log.info(logUtil.serviceBeginDivider("更新指定的权限详细信息"));
        log.info("输入参数: newAuth[{}]", JSONObject.toJSONString(newAuth));

        try {
            if (newAuth.getId() == null) {
                log.info("权限ID错误: id[{}]", newAuth.getId());
                throw new BusinessException("权限ID错误");
            }

            int affected = authDao.update(newAuth);
            log.info("已执行SQL: affectrd[{}]", affected);

            log.info(logUtil.serviceSuccessDivider());
            return affected == 1;
        } catch (DuplicateKeyException e) {
            log.info("权限标识符重复: identifier[{}]", newAuth.getIdentifier());
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new BusinessException("权限标识符重复");
        } catch (Exception e) {
            log.info(logUtil.serviceFailDivider(e.getMessage()));
            throw new DatabaseException();
        }
    }
}
