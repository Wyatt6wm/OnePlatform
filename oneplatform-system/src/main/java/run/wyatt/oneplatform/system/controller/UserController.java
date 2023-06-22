package run.wyatt.oneplatform.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import run.wyatt.oneplatform.common.cosnt.CommonConst;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.common.util.LogUtil;
import run.wyatt.oneplatform.system.model.entity.User;
import run.wyatt.oneplatform.system.model.form.LoginForm;
import run.wyatt.oneplatform.system.model.form.RegistryForm;
import run.wyatt.oneplatform.system.service.CommonService;
import run.wyatt.oneplatform.system.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wyatt
 * @date 2023/6/16 15:26
 */
@Slf4j
@Api(tags = "用户功能接口")
@RestController
@RequestMapping("/api/sys/user")
public class UserController {
    @Autowired
    private RedisTemplate<String, Object> reids;
    @Autowired
    private CommonService commonService;
    @Autowired
    private UserService userService;

    /**
     * 注册用户
     *
     * @param registryForm {username 用户名, password 密码, captchaKey 验证码KEY, captchaInput 验证码}
     * @return 详见接口文档
     */
    @ApiOperation("注册用户")
    @PostMapping("/registry")
    public R registry(@RequestBody RegistryForm registryForm) {
        LogUtil logUtil = new LogUtil("registry");
        log.info(logUtil.apiBeginDivider("注册用户"));

        try {
            Assert.notNull(registryForm, "请求参数为null");
            Assert.notNull(registryForm.getUsername(), "用户名为null");
            Assert.notNull(registryForm.getPassword(), "密码为null");
            Assert.notNull(registryForm.getCaptchaKey(), "验证码KEY为null");
            Assert.notNull(registryForm.getCaptchaInput(), "验证码为null");
        } catch (Exception e) {
            log.info(logUtil.apiFailDivider(e.getMessage()));
            return R.fail("请求参数错误");
        }

        String username = registryForm.getUsername();
        String password = registryForm.getPassword();
        String captchaKey = registryForm.getCaptchaKey();
        String captchaInput = registryForm.getCaptchaInput();
        log.info("username[{}]", username);
        log.info("password[*]");
        log.info("captchaKey[{}]", captchaKey);
        log.info("captchaInput[{}]", captchaInput);

        // 格式校验
        if (!userService.checkUsernameFormat(username)) {
            log.info(logUtil.apiFailDivider("用户名格式错误"));
            return R.fail("用户名格式错误");
        }
        if (!userService.checkPasswordFormat(password)) {
            log.info(logUtil.apiFailDivider("密码格式错误"));
            return R.fail("密码格式错误");
        }
        if (!commonService.chechCaptchaFormat(captchaInput)) {
            log.info(logUtil.apiFailDivider("验证码格式错误"));
            return R.fail("验证码格式错误");
        }
        log.info("输入参数格式校验通过");

        // 校验验证码
        try {
            commonService.checkCaptcha(captchaKey, captchaInput);
        } catch (Exception e) {
            log.info(logUtil.apiFailDivider(e.getMessage()));
            return R.fail(e.getMessage());
        }

        // 创建用户
        try {
            Long userId = userService.createUser(username, password);

            log.info(logUtil.apiFailDivider());
            return R.success();
        } catch (Exception e) {
            log.info(logUtil.apiFailDivider(e.getMessage()));
            return R.fail(e.getMessage());
        }
    }

    /**
     * 登录认证
     *
     * @param loginForm {username 用户名, password 密码, captchaKey 验证码KEY, captchaInput 验证码}
     * @return 详见接口文档
     */
    @ApiOperation("登录认证")
    @PostMapping("/login")
    public R login(@RequestBody LoginForm loginForm) {
        LogUtil logUtil = new LogUtil("login");
        log.info(logUtil.apiBeginDivider("登录认证"));

        try {
            Assert.notNull(loginForm, "请求参数为null");
            Assert.notNull(loginForm.getUsername(), "用户名为null");
            Assert.notNull(loginForm.getPassword(), "密码为null");
            Assert.notNull(loginForm.getCaptchaKey(), "验证码KEY为null");
            Assert.notNull(loginForm.getCaptchaInput(), "验证码为null");
        } catch (Exception e) {
            log.info(logUtil.apiFailDivider(e.getMessage()));
            return R.fail("请求参数错误");
        }

        // 格式校验
        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        String captchaKey = loginForm.getCaptchaKey();
        String captchaInput = loginForm.getCaptchaInput();
        log.info("username[{}]", username);
        log.info("password[*]");
        log.info("captchaKey[{}]", captchaKey);
        log.info("captchaInput[{}]", captchaInput);

        // 格式校验
        if (!userService.checkUsernameFormat(username)) {
            log.info(logUtil.apiFailDivider("用户名格式错误"));
            return R.fail("用户名格式错误");
        }
        if (!userService.checkPasswordFormat(password)) {
            log.info(logUtil.apiFailDivider("密码格式错误"));
            return R.fail("密码格式错误");
        }
        if (!commonService.chechCaptchaFormat(captchaInput)) {
            log.info(logUtil.apiFailDivider("验证码格式错误"));
            return R.fail("验证码格式错误");
        }
        log.info("输入参数格式校验通过");

        // 校验验证码
        try {
            commonService.checkCaptcha(captchaKey, captchaInput);
        } catch (Exception e) {
            log.info(logUtil.apiFailDivider(e.getMessage()));
            return R.fail(e.getMessage());
        }

        // 验证用户名密码
        User user = null;
        try {
            user = userService.verifyByUsername(username, password);
        } catch (Exception e) {
            log.info(logUtil.apiFailDivider(e.getMessage()));
            return R.fail(e.getMessage());
        }
        user.setPassword(null);
        user.setSalt(null);
        log.info("user[{}]", JSONObject.toJSONString(user));

        // 登录：Sa-Token框架自动生成token、获取角色和权限，并缓存到Redis
        StpUtil.login(user.getId());
        log.info("登录成功");
        log.info("token[{}]", StpUtil.getTokenInfo().getTokenValue());
        log.info("sessionId[{}]", StpUtil.getSession().getId());
        StpUtil.getSession().set(CommonConst.REDIS_PROFILE_KEY, user);
        log.info("保存用户详细信息到Session缓存成功");

        // 获取用户角色和权限并缓存到Redis
        List<String> roles = null;
        List<String> auths = null;
        try {
            roles = userService.listActivatedRoleIdentifiers(user.getId());
            auths = userService.listActivatedAuthIdentifiers(user.getId());
        } catch (Exception e) {
            log.info(logUtil.apiFailDivider(e.getMessage()));
            return R.fail(e.getMessage());
        }
        StpUtil.getSession().set(CommonConst.REDIS_ROLES_KEY, roles);
        StpUtil.getSession().set(CommonConst.REDIS_AUTHS_KEY, auths);
        log.info("保存角色、权限到Session缓存成功");

        // 组装响应对象
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenInfo().getTokenValue());
        data.put("tokenExpiredTime", System.currentTimeMillis() + StpUtil.getTokenActivityTimeout() * 1000);
        data.put("roles", roles);
        data.put("auths", auths);
        log.info(logUtil.apiData(JSONObject.toJSONString(data)));

        log.info(logUtil.apiSuccessDivider());
        return R.success(data);
    }

    /**
     * 获取用户详细信息。从Sa-Token框架会话中获取。
     *
     * @return 详见接口文档
     */
    @SaCheckLogin
    @ApiOperation("获取用户详细信息")
    @GetMapping("/getProfile")
    public R getProfile() {
        LogUtil logUtil = new LogUtil("getProfile");
        log.info(logUtil.apiBeginDivider("获取用户详细信息"));

        Map<String, Object> data = new HashMap<>();
        data.put("profile", StpUtil.getSession().get(CommonConst.REDIS_PROFILE_KEY));
        log.info(logUtil.apiData(JSONObject.toJSONString(data)));

        log.info(logUtil.apiSuccessDivider());
        return R.success(data);
    }

    /**
     * 退出登录
     *
     * @return 详见接口文档
     */
    @SaCheckLogin
    @ApiOperation("退出登录")
    @GetMapping("/logout")
    public R logout() {
        LogUtil logUtil = new LogUtil("logout");
        log.info(logUtil.apiBeginDivider("退出登录"));

        StpUtil.logout(StpUtil.getLoginId());

        log.info(logUtil.apiSuccessDivider());
        return R.success("退出成功");
    }
}
