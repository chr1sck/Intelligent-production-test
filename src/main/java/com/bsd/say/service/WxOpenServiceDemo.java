package com.bsd.say.service;

import com.bsd.say.config.RedisProperies;
import me.chanjar.weixin.open.api.impl.WxOpenInRedisConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenMessageRouter;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;

/**
 * @author <a href="https://github.com/007gzs">007</a>
 */
@Service
public class WxOpenServiceDemo extends WxOpenServiceImpl {
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
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static JedisPool pool;
    private WxOpenMessageRouter wxOpenMessageRouter;
    @PostConstruct
    public void init() {
        WxOpenInRedisConfigStorage inRedisConfigStorage = new WxOpenInRedisConfigStorage(getJedisPool());


        inRedisConfigStorage.getComponentVerifyTicket();




        inRedisConfigStorage.setComponentAppId(componentAppId);
        inRedisConfigStorage.setComponentAppSecret(componentAppSecret);
        inRedisConfigStorage.setComponentToken(componentToken);
        inRedisConfigStorage.setComponentAesKey(aesKey);
        setWxOpenConfigStorage(inRedisConfigStorage);
        wxOpenMessageRouter = new WxOpenMessageRouter(this);
        wxOpenMessageRouter.rule().handler((wxMpXmlMessage, map, wxMpService, wxSessionManager) -> {
            logger.info("\n接收到 {} 公众号请求消息，内容：{}", wxMpService.getWxMpConfigStorage().getAppId(), wxMpXmlMessage);
            return null;
        }).next();
    }
    public WxOpenMessageRouter getWxOpenMessageRouter(){
        return wxOpenMessageRouter;
    }

    private JedisPool getJedisPool() {
        RedisProperies redisProperies = new RedisProperies();
        if (pool == null) {
            synchronized (WxOpenServiceDemo.class) {
                if (pool == null) {
                    pool = new JedisPool(redisProperies, redisProperies.getHost(),
                            redisProperies.getPort(), redisProperies.getConnectionTimeout(),
                            redisProperies.getSoTimeout(), redisProperies.getPassword(),
                            redisProperies.getDatabase(), redisProperies.getClientName(),
                            redisProperies.isSsl(), redisProperies.getSslSocketFactory(),
                            redisProperies.getSslParameters(), redisProperies.getHostnameVerifier());
                }
            }
        }
        return pool;
    }
}
