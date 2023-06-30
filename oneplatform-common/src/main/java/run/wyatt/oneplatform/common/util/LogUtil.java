package run.wyatt.oneplatform.common.util;

import lombok.Data;

import java.util.Map;

/**
 * @author Wyatt
 * @date 2023/6/22 11:02
 */
@Data
public class LogUtil {
    private String methodName;

    public LogUtil(String methodName) {
        this.methodName = methodName;
    }

    public static String divider(String message) {
        return String.format("-------------------- %s --------------------", message);
    }
    public String apiBeginDivider() {
        return String.format("BEGIN -------------------- api[ %s ] --------------------", methodName);
    }

    public String apiBeginDivider(String function) {
        return String.format("BEGIN -------------------- api[ %s ] %s --------------------", methodName, function);
    }

    public String apiSuccessDivider() {
        return String.format("-------------------- SUCCESS api[ %s ] --------------------", methodName);
    }

    public String apiFailDivider() {
        return String.format("-------------------- FAIL api[ %s ] --------------------", methodName);
    }

    public String apiFailDivider(String reason) {
        return String.format("-------------------- FAIL api[ %s ]: %s --------------------", methodName, reason);
    }

    public String apiData(String data) {
        return String.format("响应data[%s]", data);
    }

    public String serviceBeginDivider() {
        return String.format("BEGIN ----- service[ %s ] -----", methodName);
    }

    public String serviceBeginDivider(String function) {
        return String.format("BEGIN ----- service[ %s ] %s -----", methodName, function);
    }

    public String serviceSuccessDivider() {
        return String.format("----- SUCCESS service[ %s ] -----", methodName);
    }

    public String serviceFailDivider() {
        return String.format("----- FAIL service[ %s ] -----", methodName);
    }

    public String serviceFailDivider(String reason) {
        return String.format("----- FAIL service[ %s ]: %s -----", methodName, reason);
    }
}
