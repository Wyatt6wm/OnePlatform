package run.wyatt.oneplatform.user.controller;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.http.HttpCodes;
import run.wyatt.oneplatform.common.http.HttpResult;
import run.wyatt.oneplatform.user.model.form.LoginForm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Wyatt
 * @date 2023/5/27 17:26
 */
@RestController
@RequestMapping("/api")
public class LoginController {
    // TODO 生成验证码

    @PostMapping("/login")
    public HttpResult login(@RequestBody LoginForm loginForm) {
        try {
            Assert.notNull(loginForm, "请求参数不能为空");
            Assert.hasLength();
            Assert.doesNotContain();

        String username = (String) query.get("username");
        String password = (String) query.get("password");
        // TODO 格式验证

        // TODO 校验用户名密码，同时获取用户ID

        // TODO 生成token和过期时间

        // TODO 获取用户权限

        // TODO Redis缓存关键要素（key：token，value：用户ID、token过期时间、用户权限）

        // TODO 返回响应数据
        if (username.equals("administrator") && password.equals("abc123456")) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", "d8c6ed7a3fd446e4a477b20d1ce9cda0");
            data.put("tokenExpiredTime", new Date());
            return HttpResult.success(HttpCodes.AUTHENTICATED, "认证成功", data);
        } else {
            return HttpResult.error(HttpCodes.AUTH_FAIL_USERNAME_PASSWORD_INCORRECT, "账户或密码不正确", null);
        }
    }
}
