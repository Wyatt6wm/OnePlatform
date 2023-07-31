package run.wyatt.oneplatform.util;

import java.util.UUID;

/**
 * @author Wyatt
 * @date 2023/6/12 11:20
 */
public class PasswordUtil {
    // 生成加密盐
    public static String generateSalt() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(20);
    }

    // 明文加密
    public static String encode(String plaintext, String salt) {
        return new Md5Encoder().encode(plaintext, salt);
    }
}
