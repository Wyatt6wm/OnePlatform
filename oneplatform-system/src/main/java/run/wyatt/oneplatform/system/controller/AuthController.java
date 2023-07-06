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
import org.springframework.web.bind.annotation.*;
import run.wyatt.oneplatform.common.cosnt.CommonConst;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.system.model.entity.Auth;
import run.wyatt.oneplatform.system.model.form.AuthForm;
import run.wyatt.oneplatform.system.service.AuthService;
import run.wyatt.oneplatform.system.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Wyatt
 * @date 2023/6/20 18:40
 */
@Slf4j
@Api(tags = "权限接口")
@RestController
@RequestMapping("/api/sys/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @ApiOperation("获取用户权限列表")
    @SaCheckLogin
    @GetMapping("/getAuths")
    public R getAuths() {
        // 先查询Redis
        List<String> auths = (ArrayList<String>) StpUtil.getSession().get(CommonConst.REDIS_AUTHS_KEY);
        // 如果Redis查询不到则调用远程方法查询数据库
        if (auths == null) {
            log.info("Session缓存无用户权限数据，查询数据库");
            Long userId = StpUtil.getLoginIdAsLong();
            try {
                auths = userService.listActivatedAuthIdentifiers(userId);
            } catch (Exception e) {
                return R.fail(e.getMessage());
            }
        }
        log.info("成功获取用户权限列表");

        Map<String, Object> data = new HashMap<>();
        data.put("auths", auths);

        return R.success(data);
    }

    @ApiOperation("获取用户权限详细列表")
    @SaCheckLogin
    @SaCheckRole(value = {"super_admin", "admin"}, mode = SaMode.OR)
    @GetMapping("/getAuthDetails")
    public R getAuthDetails() {

        try {
            List<Auth> authDetails = authService.listAuthDetails(StpUtil.getLoginIdAsLong());

            Map<String, Object> data = new HashMap<>();
            data.put("authDetails", authDetails);

            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("编辑权限")
    @SaCheckLogin
    @SaCheckRole("super_admin")
    @PostMapping("/editAuth")
    public R editAuth(@RequestBody AuthForm authForm) {
        log.info("输入参数: authForm={}", authForm);
        try {
            Assert.notNull(authForm, "请求参数为null");
            Assert.notNull(authForm.getId(), "权限ID为null");
        } catch (Exception e) {
            log.info(e.getMessage());
            return R.fail("请求参数错误");
        }

        Auth newAuth = new Auth();
        newAuth.setId(authForm.getId());
        newAuth.setIdentifier(authForm.getIdentifier());
        newAuth.setName(authForm.getName());
        newAuth.setDescription(authForm.getDescription());
        newAuth.setActivated(authForm.getActivated());

        try {
            authService.updateAuthDetail(newAuth);
            return R.success();
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("新增权限")
    @SaCheckLogin
    @SaCheckRole("super_admin")
    @PostMapping("/addAuth")
    public R addAuth(@RequestBody AuthForm authForm) {
        log.info("输入参数: authForm={}", authForm);
        try {
            Assert.notNull(authForm, "请求参数为null");
            Assert.hasText(authForm.getIdentifier(), "权限标识符为空");
        } catch (Exception e) {
            log.info(e.getMessage());
            return R.fail("请求参数错误");
        }

        try {
            Auth auth = new Auth();
            auth.setIdentifier(authForm.getIdentifier());
            if (authForm.getName() != null && !authForm.getName().isEmpty()) {
                auth.setName(authForm.getName());
            }
            if (authForm.getDescription() != null && !authForm.getDescription().isEmpty()) {
                auth.setDescription(authForm.getDescription());
            }
            if (authForm.getActivated() != null) {
                auth.setActivated(authForm.getActivated());
            }

            Long authId = authService.createAuth(auth);

            return R.success();
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
}
