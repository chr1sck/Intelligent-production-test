package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.config.RedisProperies;
import com.bsd.say.entities.Record;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.service.RedisService;
import com.bsd.say.util.AESWithJCEUtils;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.LogUtils;
import com.bsd.say.util.wechat.Sign;
import me.chanjar.weixin.open.api.impl.WxOpenInRedisConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenMessageRouter;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WeixinService extends WxOpenServiceImpl {


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
    @Value("${wechat.getTicketUrl}")
    private String getTicketUrl;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RecordMapper recordMapper;
    private WxOpenMessageRouter wxOpenMessageRouter;

    @Resource
    private RedisService redisService;


    Logger logger = LogUtils.getBussinessLogger();

    /**
     * ???????????????accessToken
     */

    public void refreshComponentAccessToken() {
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
            logger.info("ComponentVerifyTicket???" + ComponentVerifyTicket);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("component_appid", componentAppId);
            jsonObject.put("component_appsecret", componentAppSecret);
            jsonObject.put("component_verify_ticket", ComponentVerifyTicket);
            String post = HttpRequestUtils.sendPost(getComponentAccessTokenUrl, jsonObject);
//            logger.debug("====================??????post?????????" + post);
            HashMap<String, String> hashMap = JSON.parseObject(post, HashMap.class);
            String componentAccessToken = hashMap.get("component_access_token");
            if (StringUtils.isNotEmpty(componentAccessToken)) {
                redisTemplate.opsForValue().set("component_access_token", componentAccessToken, 60 * 60 * 2, TimeUnit.SECONDS);
                String accessToken = redisTemplate.opsForValue().get("component_access_token").toString();
                System.out.println("accessToken" + accessToken);
//                logger.debug("====================??????component_access_token?????????" + accessToken + "???====================");
            } else {
                throw new RuntimeException("????????????????????????????????????????????????????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????accessToken
     *
     * @return
     */
    public JSONObject getAccessToken(String code) {
        String component_access_token = redisTemplate.opsForValue().get("component_access_token").toString();
        String param = appId + "&code=" + code + "&grant_type=authorization_code&component_appid=" + componentAppId
                + "&component_access_token=" + component_access_token;
        String url = getAccessTokenUrl + param;
        String result = HttpRequestUtils.sendGet(url);
        JSONObject resultJson = JSONObject.parseObject(result);
        return resultJson;
    }

    /**
     * ??????unionId
     *
     * @param code
     * @return
     */
    public String getUnionId(String code) {
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
     * ??????openId??????????????????(????????????????????????)
     */
    public JSONObject getUserInfoByOpenId(String openId) {
        String result1 = HttpRequestUtils.sendGet("https://api.weq.me/wx/token.php?id=15969759463491&key=1234567890123456");
        JSONObject result2 = JSONObject.parseObject(result1);
        String result3 = result2.getString("access_token");
        String pubkey = "1234567890123456";
        String iv = "WJi7HTZQoh8eHjup";
        String decode = AESWithJCEUtils.aesDecode(result3, pubkey, iv);
        String resutl = HttpRequestUtils.sendGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + decode + "&openid=" + openId + "&lang=zh_CN");
        JSONObject jsonObject = JSONObject.parseObject(resutl);
        logger.info("userInfo:" + jsonObject.toString());
        return jsonObject;
    }

    /**
     * ??????accessToken??????ticket
     */
    public String getTicket() {
        String access_token = fetchAccessToken();
        String getTicketNewUrl = getTicketUrl + access_token + "&type=jsapi";
        String ticketResult = HttpRequestUtils.sendGet(getTicketNewUrl);
        JSONObject ticketJson = JSONObject.parseObject(ticketResult);
        String jsapi_ticket = ticketJson.getString("ticket");
//        redisTemplate.opsForValue().set("jsapi_ticket",jsapi_ticket);
        return jsapi_ticket;
    }

    public Map<String, String> getSign(String url) {
        String jsapiTicket = getTicket();
        logger.info("jsapiTicket:" + jsapiTicket);
        Map<String, String> sign = Sign.sign(jsapiTicket, url);
        sign.put("appId", appId);
        logger.info("sign:" + sign);
        return sign;
    }

    /**
     * ???????????????
     *
     * @param openId
     * @param subscribe
     */
    public void insertRecord(String openId, Integer subscribe) {
        logger.info("subscribe???" + subscribe);
        Record recordByOpenId = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getOpenId, openId)
                .and(queryWrapper1 -> queryWrapper1.eq(Record::getState, 1)));
        if (subscribe == 0) {
            //??????????????????
            if (recordByOpenId == null) {
                logger.info("????????????????????????");
                Record record = new Record();
                record.setOpenId(openId);
                record.setFan("?????????");
                record.setCreateDateTime(new Date());
                recordMapper.insert(record);
            } else {
                logger.info("??????????????????????????????");
            }
        } else {
            //??????????????????
            if (recordByOpenId == null) {
                logger.info("????????????????????????");
                Record record = new Record();
                record.setOpenId(openId);
                record.setFan("?????????");
                record.setCreateDateTime(new Date());
                recordMapper.insert(record);
            } else {
                logger.info("??????????????????,?????????");
            }
        }
    }

    public JSONObject autoLogin(String openId) {

//        String userInfo = "";
//        if (redisService.exists(openId)) {
//
//            userInfo = redisService.get(openId).toString();
//            JSONObject jsonObject = JSONObject.parseObject(userInfo);
//            Integer subscribe = jsonObject.getInteger("subscribe");
//            insertRecord(openId, subscribe);
//            return jsonObject;
//        }

        String accessToken = fetchAccessToken();
        String userInfo = HttpRequestUtils.sendGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN");
//        redisService.set(openId, userInfo.toString());
        JSONObject jsonObject = JSONObject.parseObject(userInfo);
        Integer subscribe = jsonObject.getInteger("subscribe");
        insertRecord(openId, subscribe);
        return jsonObject;
    }

    public String fetchAccessToken() {

        String access_token = "";
        //???cache?????????
        if (redisService.exists("access_token")) {
            access_token = redisService.get("access_token").toString();
            return access_token;
        }
        String accessTokenResult = HttpRequestUtils.sendGet("https://api.weq.me/wx/token.php?id=15969759463491&key=1234567890123456");
        JSONObject accessTokenObject = JSONObject.parseObject(accessTokenResult);
        String aesAccessToken = accessTokenObject.getString("access_token");
        Long expiresIn = accessTokenObject.getLongValue("expires_in");
        String pubkey = "1234567890123456";
        String iv = "WJi7HTZQoh8eHjup";
        String accessToken = AESWithJCEUtils.aesDecode(aesAccessToken, pubkey, iv);
        logger.info("-----access token" + accessToken + "expire" + expiresIn);
        redisService.set("access_token", accessToken, expiresIn - 1000);
        return accessToken;
    }


}
