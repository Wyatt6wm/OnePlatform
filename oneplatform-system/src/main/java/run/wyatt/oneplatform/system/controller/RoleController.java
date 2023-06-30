package run.wyatt.oneplatform.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.cosnt.CommonConst;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.common.util.LogUtil;
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
@Api(tags = "角色接口")
@RestController
@RequestMapping("/api/sys/role")
public class RoleController {
    @Autowired
    private UserService userService;

    @SaCheckLogin
    @ApiOperation("获取用户角色列表")
    @GetMapping("/getRoles")
    public R getRoles() {
        LogUtil logUtil = new LogUtil("getRoles");
        log.info(logUtil.apiBeginDivider("获取用户角色列表"));

        // 先查询Redis
        List<String> roles = (ArrayList<String>) StpUtil.getSession().get(CommonConst.REDIS_ROLES_KEY);
        // 如果Redis查询不到则调用远程方法查询数据库
        if (roles == null) {
            log.info("Session缓存无用户角色数据，查询数据库");
            Long userId = StpUtil.getLoginIdAsLong();
            try {
                roles = userService.listActivatedRoleIdentifiers(userId);
            } catch (Exception e) {
                log.info(logUtil.apiFailDivider(e.getMessage()));
                return R.fail(e.getMessage());
            }
        }
        log.info("成功获取用户角色列表");

        Map<String, Object> data = new HashMap<>();
        data.put("roles", roles);
        log.info(logUtil.apiData(JSONObject.toJSONString(data)));

        log.info(logUtil.apiSuccessDivider());
        return R.success(data);
    }

    public R createRole() {
        return null;
    }
}
