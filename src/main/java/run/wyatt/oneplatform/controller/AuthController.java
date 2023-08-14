package run.wyatt.oneplatform.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.model.constant.RoleConst;
import run.wyatt.oneplatform.model.entity.Auth;
import run.wyatt.oneplatform.model.entity.RoleAuth;
import run.wyatt.oneplatform.model.form.AuthForm;
import run.wyatt.oneplatform.model.http.MapData;
import run.wyatt.oneplatform.model.http.R;
import run.wyatt.oneplatform.repository.AuthRepository;
import run.wyatt.oneplatform.repository.RoleAuthRepository;
import run.wyatt.oneplatform.service.AuthService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/20 18:40
 */
@Slf4j
@Api(tags = "权限接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @ApiOperation("新增权限")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @PostMapping("/addAuth")
    public R addAuth(@RequestBody AuthForm authForm) {
        log.info("请求参数: {}", authForm);
        Assert.notNull(authForm, "请求参数为空");
        Assert.hasText(authForm.getIdentifier(), "identifier为null");

        log.info("AuthForm转换为Auth");
        Auth auth = authForm.convert();

        log.info("创建权限");
        Auth newAuth = authService.createAuth(auth);

        MapData data = new MapData();
        data.put("auth", newAuth);
        return R.success(data);
    }

    @ApiOperation("删除权限")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @GetMapping("/removeAuth/{authId}")
    public R removeAuth(@PathVariable("authId") Long authId) {
        log.info("请求参数: authId={}", authId);
        Assert.notNull(authId, "请求参数为空");

        log.info("删除权限");
        authService.removeAuth(authId);
        return R.success();
    }

    @ApiOperation("编辑权限")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @PostMapping("/editAuth")
    public R editAuth(@RequestBody AuthForm authForm) {
        log.info("请求参数: {}", authForm);
        Assert.notNull(authForm, "请求参数为空");
        Assert.notNull(authForm.getId(), "id为null");

        log.info("AuthForm转换为Auth");
        Auth auth = authForm.convert();

        log.info("更新权限");
        Auth newAuth = authService.updateAuth(auth);

        MapData data = new MapData();
        data.put("auth", newAuth);
        return R.success(data);
    }

    @ApiOperation("获取权限管理列表")
    @SaCheckLogin
    @SaCheckRole(value = {RoleConst.SUPER_ADMIN_IDENTIFIER, RoleConst.ADMIN_IDENTIFIER}, mode = SaMode.OR)
    @GetMapping("/getAuthManageList")
    public R getAuthManageList() {
        log.info("获取权限管理列表");
        List<Auth> authManageList = authService.listAuths();

        MapData data = new MapData();
        data.put("authManageList", authManageList);
        return R.success(data);
    }
}
