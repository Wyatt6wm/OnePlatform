package run.wyatt.oneplatform.system.service;

import run.wyatt.oneplatform.common.http.MapData;

/**
 * 公共服务，不属于任何一部分的、公用的服务放在这里
 *
 * @author Wyatt
 * @date 2023/6/20 22:21
 */
public interface CommonService {
    /**
     * 生成验证码
     *
     * @return {captchaKey 验证码KEY, captchaImage 验证码Base64图像}
     */
    MapData generateCaptcha();

    /**
     * 检查输入的验证码格式是否不正确
     *
     * @param captchaInput 输入的验证码
     * @return true 格式不正确 / false 格式正确
     */
    boolean wrongCaptchaFormat(String captchaInput);

    /**
     * 查验验证码，查验失败时抛出异常
     *
     * @param captchaKey   验证码KEY
     * @param captchaInput 输入的验证码
     */
    void verifyCaptcha(String captchaKey, String captchaInput);
}
