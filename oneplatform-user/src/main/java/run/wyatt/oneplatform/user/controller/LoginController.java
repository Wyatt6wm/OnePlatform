package run.wyatt.oneplatform.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import run.wyatt.oneplatform.common.http.HttpResult;
import run.wyatt.oneplatform.user.model.entity.User;
import run.wyatt.oneplatform.user.model.form.LoginForm;
import run.wyatt.oneplatform.user.service.PermissionService;
import run.wyatt.oneplatform.user.service.UserService;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Wyatt
 * @date 2023/5/27 17:26
 */
@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private Producer producer;
    @Autowired
    private UserService userService;
    @Autowired
    private PermissionService permissionService;

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

    /**
     * 登录认证
     *
     * @param request 请求HTTP对象
     * @param loginForm {username 用户名, password 密码, verifyCode验证码}
     * @return 详见接口文档
     */
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
        // TODO 更完善的格式校验

        // 校验验证码
        String verifyCode = loginForm.getVerifyCode().toLowerCase();
        Object kaptchaText = request.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
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
