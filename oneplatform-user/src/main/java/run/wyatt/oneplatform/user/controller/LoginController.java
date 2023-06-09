package run.wyatt.oneplatform.user.controller;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import run.wyatt.oneplatform.common.http.HttpResult;
import run.wyatt.oneplatform.user.model.form.LoginForm;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author Wyatt
 * @date 2023/5/27 17:26
 */
@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private Producer producer;

    // 获取验证码
    @GetMapping("/getKaptcha")
    public void kaptcha(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        request.getSession().setAttribute(Constants.KAPTCHA_SESSION_KEY, text); // 保存到session中以供登录时校验

        try {
            ServletOutputStream out = response.getOutputStream();
            ImageIO.write(image, "jpg", out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 登录
    @PostMapping("/login")
    public HttpResult login(HttpServletRequest request, @RequestBody LoginForm loginForm) {
        try {
            Assert.notNull(loginForm, "请求参数不能为空");
            Assert.hasText(loginForm.getUsername(), "用户名不能为空");
            Assert.hasText(loginForm.getPassword(), "密码不能为空");
            Assert.hasText(loginForm.getVerifyCode(), "验证码不能为空");
        } catch (Exception e) {
            return HttpResult.fail(e.getMessage());
        }

        // 校验验证码
        String verifyCode = loginForm.getVerifyCode();
        Object kaptchaText = request.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (kaptchaText == null) {
            return HttpResult.fail("验证码失效");
        }
        if (!verifyCode.equals(kaptchaText)) {
            return HttpResult.fail("验证码错误");
        }

        // TODO 校验用户名密码，同时获取用户ID、用户信息
        // TODO 获取用户权限
        // TODO 生成token和过期时间
        // TODO 缓存关键要素（key：token，value：用户ID、token过期时间、用户权限）
        // TODO 返回响应数据
//        if (username.equals("administrator") && password.equals("abc123456")) {
//            Map<String, Object> data = new HashMap<>();
//            data.put("token", "d8c6ed7a3fd446e4a477b20d1ce9cda0");
//            data.put("tokenExpiredTime", new Date());
//            return HttpResult.success(HttpCodes.AUTHENTICATED, "认证成功", data);
//        } else {
//            return HttpResult.error(HttpCodes.AUTH_FAIL_USERNAME_PASSWORD_INCORRECT, "账户或密码不正确", null);
//        }

        return null;
    }
}
