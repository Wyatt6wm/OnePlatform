package run.wyatt.oneplatform.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.wyatt.oneplatform.model.exception.BusinessException;
import run.wyatt.oneplatform.model.http.R;
import run.wyatt.oneplatform.service.CaptchaService;
import run.wyatt.oneplatform.service.IdService;

/**
 * 公共API，不属于任何一部分的、公用的API放在这里
 *
 * @author Wyatt
 * @date 2023/6/20 22:13
 */
@Slf4j
@Api(tags = "公共服务接口")
@RestController
@RequestMapping("/api/common")
public class CommonController {
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private IdService idService;

    /**
     * 获取验证码
     *
     * @return {captchaKey 验证码KEY, captchaImage 验证码Base64图像}
     */
    @ApiOperation("获取验证码")
    @GetMapping("/getCaptcha")
    public R getCaptcha() {
        try {
            return R.success(captchaService.generateCaptcha());
        } catch (Exception e) {
            throw new BusinessException("获取验证码错误");
        }
    }

    /**
     * 获取雪花算法生成的全局唯一ID
     *
     * @return ID
     */
    @ApiOperation("获取雪花ID")
    @GetMapping("/getSnowflakeId")
    public R getSnowflakeId() {
        try {
            return R.success(idService.generateSnowflakeId());
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
}
