package run.wyatt.oneplatform.system.controller;

import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.http.Data;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.common.util.ImageUtil;
import run.wyatt.oneplatform.system.model.constant.SysConst;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.UUID;

/**
 * 公共API，不属于任何一部分的、公用的API放在这里
 *
 * @author Wyatt
 * @date 2023/6/20 22:13
 */
@Slf4j
@Api(tags = "公共服务接口")
@RestController
@RequestMapping("/api/sys/common")
public class CommonController {
    @Autowired
    private Producer producer;
    @Autowired
    private RedisTemplate<String, Object> redis;

    /**
     * 获取验证码
     *
     * @return {captchaKey 验证码KEY, captchaImage 验证码Base64图像}
     */
    @ApiOperation("获取验证码")
    @GetMapping("/getCaptcha")
    public R getCaptcha() {
        // 生成验证码KEY、验证码文本，并缓存到Redis
        String captchaKey = UUID.randomUUID().toString().replaceAll("-", "");
        String captchaText = producer.createText();
        redis.opsForValue().set(SysConst.CAPTCHA_REDIS_KEY_PREFIX + captchaKey, captchaText, Duration.ofSeconds(SysConst.CAPTCHA_REDIS_EXP_60_SECS));
        // 根据文本生成图片并转成Base64格式
        BufferedImage captchaImage = producer.createImage(captchaText);
        String captchaImageBase64 = ImageUtil.bufferedImageToBase64(captchaImage, "jpeg");
        log.info("验证码KEY: {}", captchaKey);
        log.info("验证码: {}", captchaText);

        Data data = new Data();
        data.put("captchaKey", captchaKey);
        data.put("captchaImage", captchaImageBase64);
        return R.success(data);
    }
}
