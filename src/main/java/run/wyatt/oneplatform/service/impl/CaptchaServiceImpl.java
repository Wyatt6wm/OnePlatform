package run.wyatt.oneplatform.service.impl;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import run.wyatt.oneplatform.model.constant.RedisConst;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.model.http.MapData;
import run.wyatt.oneplatform.service.CaptchaService;
import run.wyatt.oneplatform.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.UUID;

/**
 * @author Wyatt
 * @date 2023/6/20 22:21
 */
@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {
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
        redis.opsForValue().set(RedisConst.CAPTCHA_KEY_PREFIX + captchaKey, captchaText, Duration.ofSeconds(RedisConst.CAPTCHA_EXP_60_SECS));
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
    public boolean wrongCaptchaFormat(String captchaInput) {
        return !captchaInput.matches(CAPTCHA_REGEXP);
    }

    @Override
    public void verifyCaptcha(String captchaKey, String captchaInput) {
        log.info("输入参数: captchaKey={}, captchaInput={}", captchaKey, captchaInput);

        Object captchaText = redis.opsForValue().get(RedisConst.CAPTCHA_KEY_PREFIX + captchaKey);
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
