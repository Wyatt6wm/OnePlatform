package run.wyatt.oneplatform.controller;

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
import run.wyatt.oneplatform.model.constant.RedisConst;
import run.wyatt.oneplatform.model.constant.RoleConst;
import run.wyatt.oneplatform.model.entity.Role;
import run.wyatt.oneplatform.model.entity.User;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.model.form.BindForm;
import run.wyatt.oneplatform.model.form.ProfileForm;
import run.wyatt.oneplatform.model.form.UserForm;
import run.wyatt.oneplatform.model.http.MapData;
import run.wyatt.oneplatform.model.http.R;
import run.wyatt.oneplatform.service.AuthService;
import run.wyatt.oneplatform.service.CaptchaService;
import run.wyatt.oneplatform.service.RoleService;
import run.wyatt.oneplatform.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/16 15:26
 */
@Slf4j
@Api(tags = "用户接口")
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthService authService;

    @ApiOperation("注册用户")
    @PostMapping("/registry")
    public R registry(@RequestBody UserForm userForm) {
        Assert.notNull(userForm, "请求参数为空");
        Assert.notNull(userForm.getUsername(), "用户名为null");
        Assert.notNull(userForm.getPassword(), "密码为null");
        Assert.notNull(userForm.getCaptchaKey(), "验证码KEY为null");
        Assert.notNull(userForm.getCaptchaInput(), "验证码为null");

        String username = userForm.getUsername();
        String password = userForm.getPassword();
        String captchaKey = userForm.getCaptchaKey();
        String captchaInput = userForm.getCaptchaInput();
        log.info("请求参数: username={}, captchaKey={}, captchaInput={}", username, captchaKey, captchaInput);

        // 格式校验
        if (userService.wrongUsernameFormat(username)) {
            throw new BusinessException("用户名格式错误");
        }
        if (userService.wrongPasswordFormat(password)) {
            throw new BusinessException("密码格式错误");
        }
        if (captchaService.wrongCaptchaFormat(captchaInput)) {
            throw new BusinessException("验证码格式错误");
        }
        log.info("输入参数格式校验通过");

        // 校验验证码
        captchaService.verifyCaptcha(captchaKey, captchaInput);

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
    public R login(@RequestBody UserForm userForm) {
        Assert.notNull(userForm, "请求参数为null");
        Assert.notNull(userForm.getUsername(), "用户名为null");
        Assert.notNull(userForm.getPassword(), "密码为null");
        Assert.notNull(userForm.getCaptchaKey(), "验证码KEY为null");
        Assert.notNull(userForm.getCaptchaInput(), "验证码为null");

        String username = userForm.getUsername();
        String password = userForm.getPassword();
        String captchaKey = userForm.getCaptchaKey();
        String captchaInput = userForm.getCaptchaInput();
        log.info("请求参数: username={}, captchaKey={}, captchaInput={}", username, captchaKey, captchaInput);

        // 格式校验
        if (userService.wrongUsernameFormat(username)) {
            throw new BusinessException("用户名格式错误");
        }
        if (userService.wrongPasswordFormat(password)) {
            throw new BusinessException("密码格式错误");
        }
        if (captchaService.wrongCaptchaFormat(captchaInput)) {
            throw new BusinessException("验证码格式错误");
        }
        log.info("输入参数格式校验通过");

        // 校验验证码
        captchaService.verifyCaptcha(captchaKey, captchaInput);

        // 验证用户名密码
        User user = userService.verifyByUsername(username, password);
        user.setPassword(null);
        user.setSalt(null);

        // 登录：Sa-Token框架自动生成token、获取角色和权限，并缓存到Redis
        StpUtil.login(user.getId());
        log.info("登录成功");
        log.info("token={}", StpUtil.getTokenInfo().getTokenValue());
        log.info("sessionId={}", StpUtil.getSession().getId());
        StpUtil.getSession().set(RedisConst.PROFILE, user);
        log.info("成功保存用户详细信息到Session缓存");

        // 获取用户角色和权限并缓存到Redis
        List<String> roles = roleService.getActivatedRoleIdentifiers(user.getId());
        List<String> auths = authService.getActivatedAuthIdentifiers(user.getId());
        StpUtil.getSession().set(RedisConst.ROLES, roles);
        StpUtil.getSession().set(RedisConst.AUTHS, auths);
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
        data.put("profile", StpUtil.getSession().get(RedisConst.PROFILE));
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
    @SaCheckRole(value = {RoleConst.SUPER_ADMIN_IDENTIFIER, RoleConst.ADMIN_IDENTIFIER}, mode = SaMode.OR)
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
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @GetMapping("/getRolesOfUser")
    public R getRolesOfUser(@RequestParam("userId") Long roleId) {
        log.info("请求参数: userId={}", roleId);
        Assert.notNull(roleId, "请求参数为空");

        log.info("查询用户的全部角色");
        List<Role> roles = roleService.listRoles(roleId);

        MapData data = new MapData();
        data.put("roles", roles);
        return R.success(data);
    }

    @ApiOperation("变更角色绑定")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
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
}
