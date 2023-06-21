package run.wyatt.oneplatform.system.model.constant;

/**
 * @author Wyatt
 * @date 2023/6/21 11:12
 */
public class SysConst {
    // ----- Redis缓存相关 -----
    public static final String CAPTCHA_REDIS_KEY_PREFIX = "kaptcha:";   // 验证码缓存KEY前缀
    public static final int CAPTCHA_REDIS_EXP_60_SECS = 60; // 验证码缓存过期时长：60秒
    // ----- 用户角色权限相关 -----
    public static final Long SUPER_ADMIN_ROLE_ID = 1L;  // 超级管理员角色ID
    public static final Long DEFAULT_ROLE_ID = 2L;  // 默认角色ID
}
