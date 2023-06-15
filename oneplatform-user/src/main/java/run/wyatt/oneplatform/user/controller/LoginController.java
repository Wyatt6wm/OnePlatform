package run.wyatt.oneplatform.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import run.wyatt.oneplatform.common.http.HttpResult;
import run.wyatt.oneplatform.common.utils.ImageUtils;
import run.wyatt.oneplatform.user.model.entity.User;
import run.wyatt.oneplatform.user.model.form.LoginForm;
import run.wyatt.oneplatform.user.service.PermissionService;
import run.wyatt.oneplatform.user.service.UserService;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Wyatt
 * @date 2023/5/27 17:26
 */
@RestController
@RequestMapping("/api")
public class LoginController {
    private static final String KAPTCHA_CACHE_PREFIX = "kaptcha:";
    private static final int KAPTCHA_EXP_SECONDS = 60;  // 过期时间1分钟

    @Autowired
    private Producer producer;
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 获取验证码
    @GetMapping("/getKaptcha")
    public HttpResult getKaptcha() {
        // 生成key、验证码，并缓存到Redis
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        String base64Image = ImageUtils.bufferedImageToBase64(image, "jpeg");
        String key = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(KAPTCHA_CACHE_PREFIX + key, text, Duration.ofSeconds(KAPTCHA_EXP_SECONDS));

        Map<String, Object> data = new HashMap<>();
        data.put("verifyCodeKey", key);
        data.put("verifyCodeImage", base64Image);
        return HttpResult.success("生成验证码成功", data);
    }

    /**
     * 登录认证
     *
     * @param loginForm {username 用户名, password 密码, verifyCode验证码}
     * @return 详见接口文档
     */
    @PostMapping("/login")
    public HttpResult login(@RequestBody LoginForm loginForm) {
        try {
            Assert.notNull(loginForm, "请求参数不能为空");
            Assert.hasText(loginForm.getUsername(), "用户名不能为空");
            Assert.hasText(loginForm.getPassword(), "密码不能为空");
            Assert.hasText(loginForm.getVerifyCodeKey(), "验证码key不能为空");
            Assert.hasText(loginForm.getVerifyCode(), "验证码不能为空");
        } catch (Exception e) {
            return HttpResult.fail(e.getMessage());
        }
        // TODO 更完善的格式校验

        // 校验验证码
        String verifyCodeKey = loginForm.getVerifyCodeKey();
        String verifyCode = loginForm.getVerifyCode().toLowerCase();
        Object kaptchaText = null;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(KAPTCHA_CACHE_PREFIX + verifyCodeKey))){
            kaptchaText = redisTemplate.opsForValue().get(KAPTCHA_CACHE_PREFIX + verifyCodeKey);
            // TODO 解决无法删除数据的问题
            //redisTemplate.delete(KAPTCHA_CACHE_PREFIX + verifyCodeKey);
        }
        if (kaptchaText == null) {
            return HttpResult.fail("验证码失效");
        }
        if (!verifyCode.equals(String.valueOf(kaptchaText).toLowerCase())) {
            return HttpResult.fail("验证码错误");
        }

        // 验证用户名密码
        User user = null;
        try {
            user = userService.verifyUserByUsername(loginForm.getUsername(), loginForm.getPassword());
        } catch (
                Exception e) {
            return HttpResult.fail(e.getMessage());
        }
        user.setPassword(null);
        user.setSalt(null);

        // 登录：Sa-Token框架自动生成token、获取角色和权限，并缓存到Redis
        StpUtil.login(user.getId());
        // TODO 缓存用户信息

        // 组装响应对象
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenInfo().getTokenValue());
        data.put("tokenExpiredTime", System.currentTimeMillis() + StpUtil.getTokenActivityTimeout() * 1000);
        data.put("roles", StpUtil.getRoleList());
        data.put("permissions", StpUtil.getPermissionList());

        return HttpResult.success("认证成功", data);
    }

    // 退出登录
    @SaCheckLogin
    @GetMapping("/logout")
    public HttpResult logout() {
        try {
            StpUtil.logout(StpUtil.getLoginId());
            return HttpResult.success("退出登录成功");
        } catch (NotLoginException e) {
            return HttpResult.success("未登录");
        }
    }
}
