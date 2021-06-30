package com.bsd.say.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt 工具类
 */
public class JWTUtil {

    // 过期时间5分钟
    private static final long EXPIRE_TIME = 10000 * 60 * 1000;
    private static final String SIGN_KEY = "de9edade0c2b85155853acf16fa2362a";

    /**
     * 用来校验token
     *
     * @param token
     * @return
     */
    public static boolean verify(String token, String key) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SIGN_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withClaim("key", key)
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }


    /**
     * 获得token中的telOrEmail
     *
     * @return token中包含的用户名
     */
    public static String getKey(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("key").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 生成签名,5min后过期
     *
     * @param type       类型
     * @param telOrEmail 手机号码或者是邮箱地址
     * @param secret     用户的密码
     * @return
     */
    public static String sign(Integer type, String telOrEmail, String secret) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        // 附带username信息
        return JWT.create()
                .withClaim("telOrEmail", telOrEmail)
                .withClaim("type", type)
                .withExpiresAt(date)
                .sign(algorithm);
    }

    /**
     * 生成签名,5min后过期
     *
     * @return
     */
    public static String sign(String key) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        Algorithm algorithm = Algorithm.HMAC256(SIGN_KEY);
        // 附带username信息
        return JWT.create()
                .withClaim("key", key)
                .withExpiresAt(date)
                .sign(algorithm);
    }


    public static void main(String[] args) {

        String key = sign("oclE4xPlZO8eUoD51BdEhBjObxFU");
        System.out.println(key);
    }

    public static String getToken(HttpServletRequest request) {

        Enumeration headerNames = request.getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map.get("token");
    }

    /**
     * 获取token中的jobNumber
     */
    public static String getJobNumber(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("jobNumber").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }
}