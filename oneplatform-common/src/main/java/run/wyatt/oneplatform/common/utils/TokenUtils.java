package run.wyatt.oneplatform.common.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Wyatt
 * @date 2023/6/12 17:43
 */
public class TokenUtils {
    private static final String SECRET = "vCr5Wdvp";    // 密钥
    private static final long EXP_DURATION = 3 * 60 * 60 * 1000;    // 有效期3小时

    public static Map<String, Object> generateToken() {
        String sub = UUID.randomUUID().toString().replaceAll("-", "");
        Date crt = new Date();
        Map<String, Object> claims = new HashMap<>();
        claims.put("crt", crt);
        Date exp = new Date(System.currentTimeMillis() + EXP_DURATION);
        String jwt = Jwts.builder().setSubject(sub).addClaims(claims).setExpiration(exp).signWith(SignatureAlgorithm.HS256, SECRET).compact();

        Map<String, Object> token = new HashMap<>();
        token.put("sub", sub);
        token.put("crt", crt);
        token.put("exp", exp);
        token.put("jwt", jwt);

        return token;
    }
}
