package run.wyatt.oneplatform.common.http;

/**
 * 响应码枚举值，详见接口文档
 *
 * @author Wyatt
 * @date 2023/5/29 16:24
 */
public interface HttpCodes {
    // ---------- 默认 ----------
    Integer DEFAULT_SUCCESS = 0;
    Integer DEFAULT_ERROR = 1;

    // ---------- 用户中心 ----------
    Integer AUTHENTICATED = 1000101;    // 认证成功
    Integer AUTH_FAIL_USERNAME_PASSWORD_INCORRECT = 1000102;    // 认证失败-用户名或密码不正确
}
