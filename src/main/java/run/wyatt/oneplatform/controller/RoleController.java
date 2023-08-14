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
import run.wyatt.oneplatform.model.entity.Role;
import run.wyatt.oneplatform.model.form.GrantForm;
import run.wyatt.oneplatform.model.form.RoleForm;
import run.wyatt.oneplatform.model.http.MapData;
import run.wyatt.oneplatform.model.http.R;
import run.wyatt.oneplatform.service.AuthService;
import run.wyatt.oneplatform.service.RoleService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/20 18:40
 */
@Slf4j
@Api(tags = "角色接口")
@RestController
@RequestMapping("/api/role")
public class RoleController {
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthService authService;

    @ApiOperation("新增角色")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @PostMapping("/addRole")
    public R addRole(@RequestBody RoleForm roleForm) {
        log.info("请求参数: {}", roleForm);
        Assert.notNull(roleForm, "请求参数为空");
        Assert.hasText(roleForm.getIdentifier(), "identifier为null");

        log.info("RoleForm转换为Role");
        Role role = roleForm.convert();

        log.info("创建角色");
        Role newRole = roleService.createRole(role);

        MapData data = new MapData();
        data.put("role", newRole);
        return R.success(data);
    }

    @ApiOperation("变更角色授权")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @PostMapping("/changeGrants")
    public R changeGrants(@RequestBody GrantForm grantForm) {
        log.info("请求参数: {}", grantForm);
        Assert.notNull(grantForm, "请求参数为空");

        List<Long> failGrant = null;
        List<Long> failUngrant = null;
        try {
            log.info("授权");
            failGrant = roleService.grant(grantForm.getRoleId(), grantForm.getGrantList());
        } catch (IllegalArgumentException e) {
            log.info("无须授权");
        }
        try {
            log.info("解除授权");
            failUngrant = roleService.ungrant(grantForm.getRoleId(), grantForm.getUngrantList());
        } catch (IllegalArgumentException e) {
            log.info("无须解除授权");
        }

        MapData data = new MapData();
        data.put("failGrant", failGrant);
        data.put("failUngrant", failUngrant);
        return R.success(data);
    }

    @ApiOperation("删除角色")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @GetMapping("/removeRole/{roleId}")
    public R removeRole(@PathVariable("roleId") Long roleId) {
        log.info("请求参数: roleId={}", roleId);
        Assert.notNull(roleId, "请求参数为空");

        log.info("删除角色");
        roleService.removeRole(roleId);
        return R.success();
    }

    @ApiOperation("编辑角色")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @PostMapping("/editRole")
    public R editRole(@RequestBody RoleForm roleForm) {
        log.info("请求参数: {}", roleForm);
        Assert.notNull(roleForm, "请求参数为空");
        Assert.notNull(roleForm.getId(), "id为null");

        log.info("RoleForm转换为Role");
        Role role = roleForm.convert();

        log.info("更新角色");
        Role newRole = roleService.updateRole(role);

        MapData data = new MapData();
        data.put("role", newRole);
        return R.success(data);
    }

    @ApiOperation("获取角色管理列表")
    @SaCheckLogin
    @SaCheckRole(value = {RoleConst.SUPER_ADMIN_IDENTIFIER, RoleConst.ADMIN_IDENTIFIER}, mode = SaMode.OR)
    @GetMapping("/getRoleManageList")
    public R getRoleManageList() {
        log.info("获取角色管理列表");
        List<Role> roleManageList = roleService.listRoles();

        MapData data = new MapData();
        data.put("roleManageList", roleManageList);
        return R.success(data);
    }

    @ApiOperation("获取角色所有权限")
    @SaCheckLogin
    @SaCheckRole(RoleConst.SUPER_ADMIN_IDENTIFIER)
    @GetMapping("/getAuthsOfRole/{roleId}")
    public R getAuthsOfRole(@PathVariable("roleId") Long roleId) {
        log.info("请求参数: roleId={}", roleId);
        Assert.notNull(roleId, "请求参数为空");

        log.info("查询角色的全部权限");
        List<Auth> auths = authService.listAuths(roleId);

        MapData data = new MapData();
        data.put("auths", auths);
        return R.success(data);
    }
}
