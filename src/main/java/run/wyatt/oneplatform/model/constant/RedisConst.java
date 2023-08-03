package run.wyatt.oneplatform.model.constant;

public class RedisConst {
    // ----- 验证码 -----
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";   // 验证码缓存KEY前缀
    public static final int CAPTCHA_EXP_60_SECS = 60; // 验证码缓存过期时长：60秒

    // ----- 角色和权限缓存刷新控制参数KEY -----
    public static final String REFRESH_ROLE_REDIS = "refreshRoleRedis"; // 用户需更新角色标识缓存的标志
    public static final String REFRESH_AUTH_REDIS = "refreshAuthRedis"; // 用户需更新权限标识缓存的标志
    public static final String ROLE_DB_CHANGE_TIME = "roleDbChangeTime"; // 角色最近更新数据库时间的缓存KEY
    public static final String AUTH_DB_CHANGE_TIME = "authDbChangeTime"; // 权限最近更新数据库时间的缓存KEY
    public static final String ROLE_REDIS_CHANGE_TIME = "roleRedisChangeTime";// 角色最近更新缓存时间的缓存KEY
    public static final String AUTH_REDIS_CHANGE_TIME = "authRedisChangeTime";// 权限最近更新缓存时间的缓存KEY

    // ----- 用户信息缓存KEY -----
    public static final String PROFILE = "profile";
    public static final String ROLES = "roles";
    public static final String AUTHS = "auths";
}
