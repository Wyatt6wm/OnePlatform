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
    private Integer code;   // 响应码
    private String mesg;    // 响应消息
    private Object data;    // 响应数据

    public static HttpResult success(Integer code, String mesg, Object data) {
        HttpResult r = new HttpResult(true, HttpCodes.DEFAULT_SUCCESS, "处理成功", null);
        if (code != null) r.setCode(code);
        if (mesg != null) r.setMesg(mesg);
        if (data != null) r.setData(data);
        return r;
    }

    public static HttpResult error(Integer code, String mesg, Object data) {
        HttpResult r = new HttpResult(false, HttpCodes.DEFAULT_ERROR, "处理失败", null);
        if (code != null) r.setCode(code);
        if (mesg != null) r.setMesg(mesg);
        if (data != null) r.setData(data);
        return r;
    }
}
