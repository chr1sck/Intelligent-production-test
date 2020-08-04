package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsd.say.util.HttpRequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinService {
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
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 刷新第三方accessToken
     */
    public void refreshComponentAccessToken(){
//        Map<String, String> reMap;
        try {
            // 核心定时器，每一个小时执行一次
            String ComponentVerifyTicket = redisTemplate.opsForValue().get("component_verify_ticket").toString();
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
}
