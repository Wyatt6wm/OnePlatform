package run.wyatt.oneplatform.system.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.common.http.R;
import run.wyatt.oneplatform.system.service.CommonService;

/**
 * 公共API，不属于任何一部分的、公用的API放在这里
 *
 * @author Wyatt
 * @date 2023/6/20 22:13
 */
@Slf4j
@Api(tags = "公共服务接口")
@RestController
@RequestMapping("/api/sys/common")
public class CommonController {
    @Autowired
    private CommonService commonService;

    /**
     * 获取验证码
     *
     * @return {captchaKey 验证码KEY, captchaImage 验证码Base64图像}
     */
    @ApiOperation("获取验证码")
    @GetMapping("/getCaptcha")
    public R getCaptcha() {
        try {
            return R.success(commonService.generateCaptcha());
        } catch (Exception e) {
            return R.fail("获取验证码错误");
        }
    }
}
