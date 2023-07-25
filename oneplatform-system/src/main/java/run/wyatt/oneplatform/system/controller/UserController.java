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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.cosnt.CommonConst;
import run.wyatt.oneplatform.common.exception.BusinessException;
import run.wyatt.oneplatform.common.http.MapData;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Role;
import run.wyatt.oneplatform.system.model.entity.User;
import run.wyatt.oneplatform.system.model.form.BindForm;
import run.wyatt.oneplatform.system.model.form.ChangePasswordForm;
import run.wyatt.oneplatform.system.model.form.LoginForm;
import run.wyatt.oneplatform.system.model.form.ProfileForm;
import run.wyatt.oneplatform.system.model.form.RegistryForm;
import run.wyatt.oneplatform.system.service.AuthService;
import run.wyatt.oneplatform.system.service.CommonService;
import run.wyatt.oneplatform.system.service.RoleService;
import run.wyatt.oneplatform.system.service.UserService;

import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthService authService;

    @ApiOperation("注册用户")
    @PostMapping("/registry")
    public R registry(@RequestBody RegistryForm registryForm) {
        Assert.notNull(registryForm, "请求参数为null");
        Assert.notNull(registryForm.getUsername(), "用户名为null");
        Assert.notNull(registryForm.getPassword(), "密码为null");
        Assert.notNull(registryForm.getCaptchaKey(), "验证码KEY为null");
        Assert.notNull(registryForm.getCaptchaInput(), "验证码为null");

        String username = registryForm.getUsername();
        String password = registryForm.getPassword();
        String captchaKey = registryForm.getCaptchaKey();
        String captchaInput = registryForm.getCaptchaInput();
        log.info("请求参数: username={}, captchaKey={}, captchaInput={}", username, captchaKey, captchaInput);

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
        Assert.notNull(loginForm, "请求参数为null");
        Assert.notNull(loginForm.getUsername(), "用户名为null");
        Assert.notNull(loginForm.getPassword(), "密码为null");
        Assert.notNull(loginForm.getCaptchaKey(), "验证码KEY为null");
        Assert.notNull(loginForm.getCaptchaInput(), "验证码为null");

        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        String captchaKey = loginForm.getCaptchaKey();
        String captchaInput = loginForm.getCaptchaInput();
        log.info("请求参数: username={}, captchaKey={}, captchaInput={}", username, captchaKey, captchaInput);

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

        // 验证用户名密码
        User user = userService.verifyByUsername(username, password);
        user.setPassword(null);
        user.setSalt(null);

        // 登录：Sa-Token框架自动生成token、获取角色和权限，并缓存到Redis
        StpUtil.login(user.getId());
        log.info("登录成功");
        log.info("token={}", StpUtil.getTokenInfo().getTokenValue());
        log.info("sessionId={}", StpUtil.getSession().getId());
        StpUtil.getSession().set(CommonConst.REDIS_PROFILE_KEY, user);
        log.info("成功保存用户详细信息到Session缓存");

        // 获取用户角色和权限并缓存到Redis
        List<String> roles = roleService.getActivatedRoleIdentifiers(user.getId());
        List<String> auths = authService.getActivatedAuthIdentifiers(user.getId());
        StpUtil.getSession().set(CommonConst.REDIS_ROLES_KEY, roles);
        StpUtil.getSession().set(CommonConst.REDIS_AUTHS_KEY, auths);
        log.info("成功保存角色、权限到Session缓存");

        // 组装响应数据
        MapData data = new MapData();
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
    @GetMapping("/getRoleIdentifiers")
    public R getRoleIdentifiers() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = roleService.getActivatedRoleIdentifiers(userId);
        MapData data = new MapData();
        data.put("roles", roles);
        return R.success(data);
    }

    @ApiOperation("查询用户权限标识")
    @SaCheckLogin
    @GetMapping("/getAuthIdentifiers")
    public R getAuthIdentifiers() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> auths = authService.getActivatedAuthIdentifiers(userId);
        MapData data = new MapData();
        data.put("auths", auths);
        return R.success(data);
    }

    @ApiOperation("获取用户信息")
    @SaCheckLogin
    @GetMapping("/getProfile")
    public R getProfile() {
        MapData data = new MapData();
        data.put("profile", StpUtil.getSession().get(CommonConst.REDIS_PROFILE_KEY));
        log.info("获取用户详细信息成功");
        return R.success(data);
    }

    @ApiOperation("修改用户信息")
    @SaCheckLogin
    @PostMapping("/editProfile")
    public R editProfile(@RequestBody ProfileForm profileForm) {
        log.info("请求参数: {}", profileForm);
        Assert.notNull(profileForm, "请求参数为null");

        Long userId = StpUtil.getLoginIdAsLong();
        String nickname = profileForm.getNickname();
        String motto = profileForm.getMotto();

        User profile = new User();
        profile.setNickname((nickname == null || nickname.isBlank()) ? null : nickname);
        profile.setMotto((motto == null || motto.isBlank()) ? null : motto);

        User newProfile = userService.editProfile(userId, profile);

        MapData data = new MapData();
        data.put("profile", newProfile);
        return R.success(data);
    }

    @ApiOperation("获取用户管理列表")
    @SaCheckLogin
    @SaCheckRole(value = {SysConst.SUPER_ADMIN_ROLE_IDENTIFIER, SysConst.ADMIN_ROLE_IDENTIFIER}, mode = SaMode.OR)
    @GetMapping("/getUserManageList")
    public R getUserManageList() {
        log.info("查询全部用户");
        List<User> userList = userService.listAllUsersNoSensitives();

        log.info("查询每个用户绑定的角色名称表");
        List<MapData> userManageList = new ArrayList<>();
        for (User user : userList) {
            List<Role> roles = roleService.listRoles(user.getId());
            MapData item = new MapData();
            item.put("user", user);
            item.put("roles", roles);
            userManageList.add(item);
        }

        MapData data = new MapData();
        data.put("userManageList", userManageList);
        return R.success(data);
    }

    @ApiOperation("获取用户所有角色")
    @SaCheckLogin
    @SaCheckRole(SysConst.SUPER_ADMIN_ROLE_IDENTIFIER)
    @GetMapping("/getRolesOfUser")
    public R getRolesOfUser(@RequestParam("userId") Long roleId) {
        log.info("请求参数: userId={}", roleId);
        Assert.notNull(roleId, "请求参数为null");

        log.info("查询用户的全部角色");
        List<Role> roles = roleService.listRoles(roleId);

        MapData data = new MapData();
        data.put("roles", roles);
        return R.success(data);
    }

    @ApiOperation("变更角色绑定")
    @SaCheckLogin
    @SaCheckRole(SysConst.SUPER_ADMIN_ROLE_IDENTIFIER)
    @PostMapping("/changeBinds")
    public R changeBinds(@RequestBody BindForm bindForm) {
        log.info("请求参数: {}", bindForm);
        Assert.notNull(bindForm, "请求参数为null");

        List<Long> failBind = userService.bind(bindForm.getUserId(), bindForm.getBindList());
        List<Long> failUnbind = userService.unbind(bindForm.getUserId(), bindForm.getUnbindList());

        MapData data = new MapData();
        data.put("failBind", failBind);
        data.put("failUnbind", failUnbind);
        return R.success(data);
    }

    @ApiOperation("修改密码")
    @SaCheckLogin
    @PostMapping("/changePassword")
    public R changePassword(@RequestBody ChangePasswordForm form) {
        Assert.notNull(form, "请求参数为null");
        Assert.notNull(form.getUserId(), "用户ID为null");
        Assert.notNull(form.getPassword(), "密码为null");
        Assert.notNull(form.getNewPassword(), "新密码为null");
        Assert.notNull(form.getCaptchaKey(), "验证码KEY为null");
        Assert.notNull(form.getCaptchaInput(), "验证码为null");

        Long userId = form.getUserId();
        String password = form.getPassword();
        String newPassword = form.getNewPassword();
        String captchaKey = form.getCaptchaKey();
        String captchaInput = form.getCaptchaInput();
        log.info("请求参数: userId={}, captchaKey={}, captchaInput={}", userId, captchaKey, captchaInput);

        // 格式校验
        if (userService.wrongPasswordFormat(password)) {
            throw new BusinessException("密码格式错误");
        }
        if (userService.wrongPasswordFormat(newPassword)) {
            throw new BusinessException("新密码格式错误");
        }
        if (commonService.wrongCaptchaFormat(captchaInput)) {
            throw new BusinessException("验证码格式错误");
        }
        log.info("输入参数格式校验通过");

        // 校验验证码
        commonService.verifyCaptcha(captchaKey, captchaInput);

        // 验证用户名密码
        User user = userService.verifyById(userId, password);
        user.setPassword(null);
        user.setSalt(null);

        // 修改密码
        userService.changePassword(userId, newPassword);

        return R.success();
    }
}
