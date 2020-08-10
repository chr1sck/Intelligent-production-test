package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsd.say.config.RedisProperies;
import com.bsd.say.service.WxOpenServiceDemo;
import com.bsd.say.util.AESWithJCEUtils;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.LogUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import me.chanjar.weixin.open.api.impl.WxOpenInRedisConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenMessageRouter;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinService  extends WxOpenServiceImpl {
    @Value("${wechat.aesKey}")
    private String aesKey;
    @Value("${wechat.componentToken}")
    private String componentToken;
    @Value("${wechat.appId}")
    private String appId;
    @Value("${wechat.componentAppId}")
    private String componentAppId;
    @Value("${wechat.componentAppSecret}")
    private String componentAppSecret;
    @Value("${wechat.getComponentAccessTokenUrl}")
    private String getComponentAccessTokenUrl;
    @Value("${wechat.getAccessTokenUrl}")
    private String getAccessTokenUrl;
    @Value("${wechat.getUnionIdUrl}")
    private String getUnionIdUrl;
    @Value("${wechat.getWxUserInfoUrl}")
    private String getWxUserInfoUrl;
    @Resource
    private RedisTemplate redisTemplate;

    private WxOpenMessageRouter wxOpenMessageRouter;
    Logger logger = LogUtils.getBussinessLogger();
    /**
     * 刷新第三方accessToken
     */

    public void refreshComponentAccessToken(){
        RedisProperies redisProperies = new RedisProperies();
        JedisPool pool =
                    new JedisPool(redisProperies, redisProperies.getHost(),
                            redisProperies.getPort(), redisProperies.getConnectionTimeout(),
                            redisProperies.getSoTimeout(), redisProperies.getPassword(),
                            redisProperies.getDatabase(), redisProperies.getClientName(),
                            redisProperies.isSsl(), redisProperies.getSslSocketFactory(),
                            redisProperies.getSslParameters(), redisProperies.getHostnameVerifier());
        try {
            WxOpenInRedisConfigStorage inRedisConfigStorage = new WxOpenInRedisConfigStorage(pool);
            inRedisConfigStorage.setComponentAppId(componentAppId);
            inRedisConfigStorage.setComponentAppSecret(componentAppSecret);
            inRedisConfigStorage.setComponentToken(componentToken);
            inRedisConfigStorage.setComponentAesKey(aesKey);
            setWxOpenConfigStorage(inRedisConfigStorage);
            wxOpenMessageRouter = new WxOpenMessageRouter(this);
            String ComponentVerifyTicket = inRedisConfigStorage.getComponentVerifyTicket();
            logger.info("ComponentVerifyTicket："+ComponentVerifyTicket);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("component_appid", componentAppId);
            jsonObject.put("component_appsecret", componentAppSecret);
            jsonObject.put("component_verify_ticket", ComponentVerifyTicket);
            String post = HttpRequestUtils.sendPost(getComponentAccessTokenUrl,jsonObject);
//            logger.debug("====================返回post结果：" + post);
            HashMap<String, String> hashMap = JSON.parseObject(post, HashMap.class);
            String componentAccessToken = hashMap.get("component_access_token");
            if (StringUtils.isNotEmpty(componentAccessToken)) {
                redisTemplate.opsForValue().set("component_access_token", componentAccessToken, 60 * 60 * 2, TimeUnit.SECONDS);
                String accessToken = redisTemplate.opsForValue().get("component_access_token").toString();
                System.out.println("accessToken"+accessToken);
//                logger.debug("====================令牌component_access_token】：【" + accessToken + "】====================");
            } else {
                throw new RuntimeException("微信开放平台，第三方平台获取【令牌】失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取accessToken
     * @return
     */
    public JSONObject getAccessToken(String code){
        String component_access_token =  redisTemplate.opsForValue().get("component_access_token").toString();
        String param = appId + "&code=" + code + "&grant_type=authorization_code&component_appid=" + componentAppId
                + "&component_access_token=" + component_access_token;
        String url = getAccessTokenUrl + param;
        String result = HttpRequestUtils.sendGet(url);
        JSONObject resultJson = JSONObject.parseObject(result);
        return resultJson;
    }

    /**
     * 获取unionId
     * @param code
     * @return
     */
    public String getUnionId(String code){
        JSONObject jsonObject = getAccessToken(code);
        String accessToken = jsonObject.getString("access_token");
        String openid = jsonObject.getString("openid");
        String param = accessToken + "&openid=" + openid + "&lang=zh_CN";
        String url = getUnionIdUrl + param;
        String result = HttpRequestUtils.sendGet(url);
        JSONObject resultJson = JSONObject.parseObject(result);
        String unionId = resultJson.getString("unionid");
        return unionId;
    }

    /**
     * 通过openId获取用户信息(含是否订阅公众号)
     */
    public JSONObject getUserInfoByOpenId(String openId){
        String result1 = HttpRequestUtils.sendGet("https://api.weq.me/wx/token.php?id=15969759463491&key=1234567890123456");
        logger.info("openId----: "+ openId);
        JSONObject result2 = JSONObject.parseObject(result1);
        String result3 = result2.getString("access_token");
        String pubkey = "1234567890123456";
        String iv = "WJi7HTZQoh8eHjup";
        String decode = AESWithJCEUtils.aesDecode(result3, pubkey, iv);
        String resutl = HttpRequestUtils.sendGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + decode + "&openid=" + openId + "&lang=zh_CN");
        JSONObject jsonObject = JSONObject.parseObject(resutl);
        logger.info("userInfo:"+ jsonObject.toString());
        return jsonObject;
    }
}
