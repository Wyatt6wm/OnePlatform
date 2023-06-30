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
@Api(tags = "权限接口")
@RestController
@RequestMapping("/api/sys/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @SaCheckLogin
    @ApiOperation("获取用户权限列表")
    @GetMapping("/getAuths")
    public R getAuths() {
        LogUtil logUtil = new LogUtil("getAuths");
        log.info(logUtil.apiBeginDivider("获取用户权限列表"));

        // 先查询Redis
        List<String> auths = (ArrayList<String>) StpUtil.getSession().get(CommonConst.REDIS_AUTHS_KEY);
        // 如果Redis查询不到则调用远程方法查询数据库
        if (auths == null) {
            log.info("Session缓存无用户权限数据，查询数据库");
            Long userId = StpUtil.getLoginIdAsLong();
            try {
                auths = userService.listActivatedAuthIdentifiers(userId);
            } catch (Exception e) {
                log.info(logUtil.apiFailDivider(e.getMessage()));
                return R.fail(e.getMessage());
            }
        }
        log.info("成功获取用户权限列表");

        Map<String, Object> data = new HashMap<>();
        data.put("auths", auths);
        log.info(logUtil.apiData(JSONObject.toJSONString(data)));

        log.info(logUtil.apiSuccessDivider());
        return R.success(data);
    }

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
