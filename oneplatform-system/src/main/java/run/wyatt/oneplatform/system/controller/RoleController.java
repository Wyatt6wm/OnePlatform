package run.wyatt.oneplatform.system.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.http.R;

/**
 * @author Wyatt
 * @date 2023/6/20 18:40
 */
@Slf4j
@Api(tags = "角色接口")
@RestController
@RequestMapping("/api/sys/role")
public class RoleController {
    public R createRole() {
        return null;
    }
}
