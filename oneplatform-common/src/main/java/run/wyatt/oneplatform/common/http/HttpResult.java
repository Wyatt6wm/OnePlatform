package run.wyatt.oneplatform.common.http;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 响应对象封装，遵循前后端接口规范
 *
 * @author Wyatt
 * @date 2023/5/29 15:41
 */
@Data
@AllArgsConstructor
public class HttpResult {
    private Boolean succ;   // 业务处理成功标志true/false
    private String mesg;    // 响应消息
    private Object data;    // 响应数据

    public static HttpResult success(String mesg, Object data) {
        return new HttpResult(true, mesg, data);
    }

    public static HttpResult success(String mesg) {
        return new HttpResult(true, mesg, null);
    }

    public static HttpResult success(Object data) {
        return new HttpResult(true, "处理成功", data);
    }

    public static HttpResult success() {
        return new HttpResult(true, "处理成功", null);
    }

    public static HttpResult fail(String mesg, Object data) {
        return new HttpResult(false, mesg, data);
    }

    public static HttpResult fail(String mesg) {
        return new HttpResult(false, mesg, null);
    }

    public static HttpResult fail(Object data) {
        return new HttpResult(false, "处理失败", data);
    }

    public static HttpResult fail() {
        return new HttpResult(false, "处理失败", null);
    }
}
