package run.wyatt.oneplatform.system.service.impl;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.http.MapData;
import run.wyatt.oneplatform.common.util.ImageUtil;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.service.CommonService;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.UUID;

/**
 * @author Wyatt
 * @date 2023/6/20 22:21
 */
@Slf4j
@Service
public class CommonServiceImpl implements CommonService {
    private static final String CAPTCHA_REGEXP = "^[A-Za-z0-9]{5}$";
    @Autowired
    private Producer producer;
    @Autowired
    private RedisTemplate<String, Object> redis;

    @Override
    public MapData generateCaptcha() {
        // 生成验证码KEY、验证码文本，并缓存到Redis
        String captchaKey = UUID.randomUUID().toString().replaceAll("-", "");
        String captchaText = producer.createText();
        redis.opsForValue().set(SysConst.CAPTCHA_REDIS_KEY_PREFIX + captchaKey, captchaText, Duration.ofSeconds(SysConst.CAPTCHA_REDIS_EXP_60_SECS));
        // 根据文本生成图片并转成Base64格式
        BufferedImage captchaImage = producer.createImage(captchaText);
        String captchaImageBase64 = ImageUtil.bufferedImageToBase64(captchaImage, "jpeg");
        log.info("验证码KEY: {}", captchaKey);
        log.info("验证码: {}", captchaText);

        MapData data = new MapData();
        data.put("captchaKey", captchaKey);
        data.put("captchaImage", captchaImageBase64);
        return data;
    }

    @Override
    public boolean invalidCaptchaFormat(String captchaInput) {
        return !captchaInput.matches(CAPTCHA_REGEXP);
    }

    @Override
    public void checkCaptcha(String captchaKey, String captchaInput) {
        log.info("输入参数: captchaKey={}, captchaInput={}", captchaKey, captchaInput);

        Object captchaText = redis.opsForValue().get(SysConst.CAPTCHA_REDIS_KEY_PREFIX + captchaKey);
        if (captchaText == null) {
            throw new BusinessException("验证码失效");
        } else {
            log.info("缓存验证码: captchaText={}", captchaText);
            if (captchaInput.equalsIgnoreCase(String.valueOf(captchaText))) {
                log.info("通过验证");
            } else {
                throw new BusinessException("验证码错误");
            }
        }
    }
}
