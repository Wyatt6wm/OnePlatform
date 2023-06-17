package run.wyatt.oneplatform.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.cosnt.CacheConst;
import run.wyatt.oneplatform.common.http.HttpResult;
import run.wyatt.oneplatform.system.model.form.SignUpForm;

/**
 * @author Wyatt
 * @date 2023/6/16 15:26
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/signUp")
    public HttpResult signUp(@RequestBody SignUpForm signUpForm) {
        try {
            Assert.notNull(signUpForm, "请求参数不能为空");
            Assert.hasText(signUpForm.getUsername(), "用户名不能为空");
            Assert.hasText(signUpForm.getPassword(), "密码不能为空");
            Assert.hasText(signUpForm.getVerifyCodeKey(), "验证码key不能为空");
            Assert.hasText(signUpForm.getVerifyCode(), "验证码不能为空");
        } catch (Exception e) {
            return HttpResult.fail(e.getMessage());
        }
        // TODO 更完善的格式校验

        // 校验验证码
        String verifyCodeKey = signUpForm.getVerifyCodeKey();
        String verifyCode = signUpForm.getVerifyCode().toLowerCase();
        Object kaptchaText = null;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(CacheConst.KAPTCHA_PREFIX + verifyCodeKey))) {
            kaptchaText = redisTemplate.opsForValue().get(CacheConst.KAPTCHA_PREFIX + verifyCodeKey);
            // TODO 解决无法删除数据的问题
            //redisTemplate.delete(CacheConst.KAPTCHA_PREFIX + verifyCodeKey);
        }
        if (kaptchaText == null) {
            return HttpResult.fail("验证码失效");
        }
        if (!verifyCode.equals(String.valueOf(kaptchaText).toLowerCase())) {
            return HttpResult.fail("验证码错误");
        }

        return null;
    }
}
