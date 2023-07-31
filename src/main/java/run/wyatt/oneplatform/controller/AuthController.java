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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.model.constant.RoleConst;
import run.wyatt.oneplatform.model.entity.Auth;
import run.wyatt.oneplatform.model.form.AuthForm;
import run.wyatt.oneplatform.model.http.MapData;
import run.wyatt.oneplatform.model.http.R;
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
        Assert.notNull(authForm, "请求参数为null");
        Assert.hasText(authForm.getIdentifier(), "权限标识符为空");

        String identifier = authForm.getIdentifier();
        String name = authForm.getName();
        String description = authForm.getDescription();
        Boolean activated = authForm.getActivated();

        Auth auth = new Auth();
        auth.setIdentifier(identifier.trim());
        auth.setName((name != null) ? name.trim() : null);
        auth.setDescription((description != null) ? description.trim() : null);
        auth.setActivated(activated);

        log.info("完成组装，新增权限");
        Auth newAuth = authService.createAuth(auth);

        MapData data = new MapData();
        data.put("auth", newAuth);
        return R.success(data);
    }

    @ApiOperation("删除权限")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @GetMapping("/removeAuth")
    public R removeAuth(@RequestParam("authId") Long authId) {
        log.info("请求参数: authId={}", authId);
        Assert.notNull(authId, "权限ID为null");

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
        Assert.notNull(authForm, "请求参数为null");
        Assert.notNull(authForm.getId(), "权限ID为null");

        Long id = authForm.getId();
        String identifier = authForm.getIdentifier();
        String name = authForm.getName();
        String description = authForm.getDescription();
        Boolean activated = authForm.getActivated();

        Auth auth = new Auth();
        auth.setIdentifier((identifier != null) ? identifier.trim() : null);
        auth.setName((name != null) ? name.trim() : null);
        auth.setDescription((description != null) ? description.trim() : null);
        auth.setActivated(activated);

        log.info("完成组装，更新权限");
        Auth newAuth = authService.updateAuth(id, auth);

        MapData data = new MapData();
        data.put("auth", newAuth);
        return R.success(data);
    }

    @ApiOperation("获取权限管理列表")
    @SaCheckLogin
    @SaCheckRole(value = {RoleConst.SUPER_ADMIN_IDENTIFIER, RoleConst.ADMIN_IDENTIFIER}, mode = SaMode.OR)
    @GetMapping("/getAuthManageList")
    public R getAuthManageList() {
        log.info("查询全部权限");
        List<Auth> authManageList = authService.listAuths();

        MapData data = new MapData();
        data.put("authManageList", authManageList);
        return R.success(data);
    }
}
