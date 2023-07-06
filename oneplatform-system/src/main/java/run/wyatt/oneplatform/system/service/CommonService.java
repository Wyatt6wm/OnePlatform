package run.wyatt.oneplatform.system.service;

/**
 * 公共服务，不属于任何一部分的、公用的服务放在这里
 *
 * @author Wyatt
 * @date 2023/6/20 22:21
 */
public interface CommonService {
    /**
     * 检查输入的验证码是否正确
     *
     * @param captchaKey   验证码KEY
     * @param captchaInput 输入的验证码
     * @return true / BusinessException
     */
    boolean checkCaptcha(String captchaKey, String captchaInput);

    /**
     * 检查输入的验证码格式是否不正确
     *
     * @param captchaInput 输入的验证码
     * @return true 格式不正确 / false 格式正确
     */
    boolean invalidCaptchaFormat(String captchaInput);
}