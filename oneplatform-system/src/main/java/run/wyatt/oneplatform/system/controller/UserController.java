package run.wyatt.oneplatform.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.cosnt.CommonConst;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.http.Data;
import run.wyatt.oneplatform.common.http.MapData;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.User;
import run.wyatt.oneplatform.system.model.form.LoginForm;
import run.wyatt.oneplatform.system.model.form.ProfileForm;
import run.wyatt.oneplatform.system.model.form.RegistryForm;
import run.wyatt.oneplatform.system.service.CommonService;
import run.wyatt.oneplatform.system.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wyatt
 * @date 2023/6/16 15:26
 */
@Slf4j
@Api(tags = "用户接口")
@RestController
@RequestMapping("/api/sys/user")
public class UserController {
    @Autowired
    private CommonService commonService;
    @Autowired
    private UserService userService;

    @ApiOperation("注册用户")
    @PostMapping("/registry")
    public R registry(@RequestBody RegistryForm registryForm) {
        try {
            Assert.notNull(registryForm, "请求参数为null");
            Assert.notNull(registryForm.getUsername(), "用户名为null");
            Assert.notNull(registryForm.getPassword(), "密码为null");
            Assert.notNull(registryForm.getCaptchaKey(), "验证码KEY为null");
            Assert.notNull(registryForm.getCaptchaInput(), "验证码为null");
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new BusinessException("请求参数错误");
        }

        String username = registryForm.getUsername();
        String password = registryForm.getPassword();
        String captchaKey = registryForm.getCaptchaKey();
        String captchaInput = registryForm.getCaptchaInput();
        log.info("请求参数: username={}, password=*, captchaKey={}, captchaInput={}", username, captchaKey, captchaInput);

        // 格式校验
        if (userService.wrongUsernameFormat(username)) {
            throw new BusinessException("用户名格式错误");
        }
        if (userService.wrongPasswordFormat(password)) {
            throw new BusinessException("密码格式错误");
        }
        if (commonService.wrongCaptchaFormat(captchaInput)) {
            throw new BusinessException("验证码格式错误");
        }
        log.info("输入参数格式校验通过");

        // 校验验证码
        commonService.verifyCaptcha(captchaKey, captchaInput);

        // 创建用户
        User user = userService.createUser(username, password);
        user.setPassword(null);
        user.setSalt(null);

        MapData data = new MapData();
        data.put("user", user);
        return R.success(data);
    }

    @ApiOperation("登录认证")
    @PostMapping("/login")
    public R login(@RequestBody LoginForm loginForm) {
        try {
            Assert.notNull(loginForm, "请求参数为null");
            Assert.notNull(loginForm.getUsername(), "用户名为null");
            Assert.notNull(loginForm.getPassword(), "密码为null");
            Assert.notNull(loginForm.getCaptchaKey(), "验证码KEY为null");
            Assert.notNull(loginForm.getCaptchaInput(), "验证码为null");
        } catch (Exception e) {
            log.info(e.getMessage());
            return R.fail("请求参数错误");
        }

        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        String captchaKey = loginForm.getCaptchaKey();
        String captchaInput = loginForm.getCaptchaInput();
        log.info("请求参数: username={}, password=*, captchaKey={}, captchaInput={}", username, captchaKey, captchaInput);

        // 格式校验
        if (userService.wrongUsernameFormat(username)) {
            return R.fail("用户名格式错误");
        }
        if (userService.wrongPasswordFormat(password)) {
            return R.fail("密码格式错误");
        }
        if (commonService.wrongCaptchaFormat(captchaInput)) {
            return R.fail("验证码格式错误");
        }
        log.info("输入参数格式校验通过");

        // 校验验证码
        try {
            commonService.verifyCaptcha(captchaKey, captchaInput);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }

        // 验证用户名密码
        User user = null;
        try {
            user = userService.verifyByUsername(username, password);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        user.setPassword(null);
        user.setSalt(null);
        log.info("user={}", user);

        // 登录：Sa-Token框架自动生成token、获取角色和权限，并缓存到Redis
        StpUtil.login(user.getId());
        log.info("登录成功");
        log.info("token={}", StpUtil.getTokenInfo().getTokenValue());
        log.info("sessionId={}", StpUtil.getSession().getId());
        StpUtil.getSession().set(CommonConst.REDIS_PROFILE_KEY, user);
        log.info("成功保存用户详细信息到Session缓存");

        // 获取用户角色和权限并缓存到Redis
        List<String> roles = null;
        List<String> auths = null;
        try {
            roles = userService.getRoleIdentifiersOfUser(user.getId());
            auths = userService.getAuthIdentifiersOfUser(user.getId());
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
        StpUtil.getSession().set(CommonConst.REDIS_ROLES_KEY, roles);
        StpUtil.getSession().set(CommonConst.REDIS_AUTHS_KEY, auths);
        log.info("成功保存角色、权限到Session缓存");

        // 组装响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenInfo().getTokenValue());
        data.put("tokenExpiredTime", System.currentTimeMillis() + StpUtil.getTokenActivityTimeout() * 1000);
        data.put("roles", roles);
        data.put("auths", auths);

        return R.success(data);
    }

    @ApiOperation("退出登录")
    @GetMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.success();
    }

    @ApiOperation("查询用户角色标识")
    @SaCheckLogin
    @GetMapping("/getRolesOfUser")
    public R getRolesOfUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            List<String> roles = userService.getRoleIdentifiersOfUser(userId);
            Data data = new Data();
            data.put("roles", roles);
            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("查询用户权限标识")
    @SaCheckLogin
    @GetMapping("/getAuthsOfUser")
    public R getAuthsOfUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            List<String> auths = userService.getAuthIdentifiersOfUser(userId);
            Data data = new Data();
            data.put("auths", auths);
            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("获取用户详细信息")
    @SaCheckLogin
    @GetMapping("/getProfile")
    public R getProfile() {
        Data data = new Data();
        data.put("profile", StpUtil.getSession().get(CommonConst.REDIS_PROFILE_KEY));
        log.info("获取用户详细信息成功");
        return R.success(data);
    }

    @ApiOperation("修改用户信息")
    @SaCheckLogin
    @PostMapping("/editProfile")
    public R editProfile(@RequestBody ProfileForm profileForm) {
        log.info("请求参数: {}", profileForm);
        try {
            Assert.notNull(profileForm, "请求参数为null");
        } catch (Exception e) {
            log.info(e.getMessage());
            return R.fail("请求参数错误");
        }

        Long userId = StpUtil.getLoginIdAsLong();

        User user = new User();
        user.setNickname(profileForm.getNickname().isEmpty() ? null : profileForm.getNickname());
        user.setMotto(profileForm.getMotto().isEmpty() ? null : profileForm.getMotto());
        try {
            User result = userService.editProfile(userId, user);
            Data data = new Data();
            data.put("profile", result);
            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("获取用户列表")
    @SaCheckLogin
    @SaCheckRole(value = {SysConst.SUPER_ADMIN_ROLE_IDENTIFIER, SysConst.ADMIN_ROLE_IDENTIFIER}, mode = SaMode.OR)
    @GetMapping("/getUserList")
    public R getUserList() {
        try {
            log.info("查询全部用户");
            List<User> userList = userService.listAllUsersDesensitized();

            log.info("查询每个用户绑定的权限名称列表");
            List<Data> list = new ArrayList<>();
            for (User user : userList) {
                List<String> identifierList = userService.getRoleIdentifiersOfUser(user.getId());
                Data item = new Data();
                item.put("user", user);
                item.put("roleNames", identifierList);
                list.add(item);
            }

            Data data = new Data();
            data.put("userList", list);
            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
}
