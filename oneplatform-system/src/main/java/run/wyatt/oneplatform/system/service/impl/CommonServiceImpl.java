package run.wyatt.oneplatform.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.service.CommonService;

/**
 * @author Wyatt
 * @date 2023/6/20 22:21
 */
@Service
public class CommonServiceImpl implements CommonService {
    private static final String CAPTCHA_REGEXP = "^[A-Za-z0-9]{5}$";

    @Autowired
    private RedisTemplate<String, Object> redis;

    @Override
    public boolean checkCaptcha(String captchaKey, String captchaInput) {
        Object captchaText = redis.opsForValue().get(SysConst.CAPTCHA_REDIS_KEY_PREFIX + captchaKey);
        if (captchaInput == null) {
            throw new BusinessException("验证码失效");
        } else {
            if (captchaText.equals(String.valueOf(captchaInput).toLowerCase())) {
                return true;
            } else {
                throw new BusinessException("验证码错误");
            }
        }
    }

    @Override
    public boolean chechCaptchaFormat(String captchaInput) {
        return captchaInput.matches(CAPTCHA_REGEXP);
    }
}
