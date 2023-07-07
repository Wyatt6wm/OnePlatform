package run.wyatt.oneplatform.system.controller;

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
import run.wyatt.oneplatform.common.http.Data;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.system.model.constant.SysConst;
import run.wyatt.oneplatform.system.model.entity.Role;
import run.wyatt.oneplatform.system.model.form.RoleForm;
import run.wyatt.oneplatform.system.service.RoleService;

import java.util.List;

/**
 * @author Wyatt
 * @date 2023/6/20 18:40
 */
@Slf4j
@Api(tags = "角色接口")
@RestController
@RequestMapping("/api/sys/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @ApiOperation("新增角色")
    @SaCheckLogin
    @SaCheckRole(SysConst.SUPER_ADMIN_ROLE_IDENTIFIER)
    @PostMapping("/addRole")
    public R addRole(@RequestBody RoleForm roleForm) {
        log.info("请求参数: roleForm={}", roleForm);
        try {
            Assert.notNull(roleForm, "请求参数为null");
            Assert.hasText(roleForm.getIdentifier(), "角色标识符为空");
        } catch (Exception e) {
            log.info(e.getMessage());
            return R.fail("请求参数错误");
        }

        String identifier = roleForm.getIdentifier();
        String name = roleForm.getName();
        String description = roleForm.getDescription();
        Boolean activated = roleForm.getActivated();

        try {
            Role role = new Role();
            role.setIdentifier(identifier.trim());
            role.setName((name != null) ? name.trim() : null);
            role.setDescription((description != null) ? description.trim() : null);
            role.setActivated(activated);
            log.info("组装后的新角色记录: {}", role);

            log.info("新增角色");
            Role result = roleService.createRole(role);

            Data data = new Data();
            data.put("role", result);
            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("删除角色")
    @SaCheckLogin
    @SaCheckRole(SysConst.SUPER_ADMIN_ROLE_IDENTIFIER)
    @GetMapping("/removeRole")
    public R removeRole(@RequestParam("id") Long roleId) {
        log.info("请求参数: id={}", roleId);
        try {
            Assert.notNull(roleId, "角色ID为null");
        } catch (Exception e) {
            log.info(e.getMessage());
            return R.fail("请求参数错误");
        }

        try {
            log.info("删除角色");
            roleService.removeRole(roleId);
            return R.success();
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("编辑角色")
    @SaCheckLogin
    @SaCheckRole(SysConst.SUPER_ADMIN_ROLE_IDENTIFIER)
    @PostMapping("/editRole")
    public R editRole(@RequestBody RoleForm roleForm) {
        log.info("请求参数: roleForm={}", roleForm);
        try {
            Assert.notNull(roleForm, "请求参数为null");
            Assert.notNull(roleForm.getId(), "角色ID为null");
        } catch (Exception e) {
            log.info(e.getMessage());
            return R.fail("请求参数错误");
        }

        Long id = roleForm.getId();
        String identifier = roleForm.getIdentifier();
        String name = roleForm.getName();
        String description = roleForm.getDescription();
        Boolean activated = roleForm.getActivated();

        try {
            Role role = new Role();
            role.setIdentifier((identifier != null) ? identifier.trim() : null);
            role.setName((name != null) ? name.trim() : null);
            role.setDescription((description != null) ? description.trim() : null);
            role.setActivated(activated);
            log.info("组装后的新角色记录: {}", role);

            log.info("更新角色");
            Role result = roleService.updateRole(id, role);

            Data data = new Data();
            data.put("role", result);
            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @ApiOperation("获取角色列表")
    @SaCheckLogin
    @SaCheckRole(value = {SysConst.SUPER_ADMIN_ROLE_IDENTIFIER, SysConst.ADMIN_ROLE_IDENTIFIER}, mode = SaMode.OR)
    @GetMapping("/getRoleList")
    public R getRoleList() {
        try {
            log.info("查询全部角色");
            List<Role> roleList = roleService.listAllRoles();

            Data data = new Data();
            data.put("roleList", roleList);
            return R.success(data);
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }
}
