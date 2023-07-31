package run.wyatt.oneplatform.http;

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
public class R {
    private Boolean succ;   // 业务处理成功标志true/false
    private String mesg;    // 日志跟踪ID
    private Object data;    // 响应数据
    private String traceId; // 日志跟踪ID

    public static R success(String mesg, Object data) {
        return new R(true, mesg, data, null);
    }

    public static R success(String mesg) {
        return new R(true, mesg, null, null);
    }

    public static R success(Object data) {
        return new R(true, null, data, null);
    }

    public static R success() {
        return new R(true, null, null, null);
    }

    public static R fail(String mesg, Object data) {
        return new R(false, mesg, data, null);
    }

    public static R fail(String mesg) {
        return new R(false, mesg, null, null);
    }

    public static R fail(Object data) {
        return new R(false, null, data, null);
    }

    public static R fail() {
        return new R(false, null, null, null);
    }
}
