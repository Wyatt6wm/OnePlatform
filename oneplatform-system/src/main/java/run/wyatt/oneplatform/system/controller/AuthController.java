package run.wyatt.oneplatform.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.http.R;

/**
 * @author Wyatt
 * @date 2023/6/20 18:40
 */
@RestController
@RequestMapping("/api/sys/auth")
public class AuthController {
    /**
     * 获取角色所绑定的权限
     *
     * @param roleId 角色ID
     * @return
     */
    @GetMapping
    public R listAllByRoleId(Long roleId) {
        return null;
    }
}
