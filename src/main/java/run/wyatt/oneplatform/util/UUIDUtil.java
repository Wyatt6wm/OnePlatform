package run.wyatt.oneplatform.util;

import java.util.UUID;

public class UUIDUtil {
    public static String generateUuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
