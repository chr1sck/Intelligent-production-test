package com.bsd.say.quartz;

import com.bsd.say.service.impl.WeixinService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 定时任务类
 */
@Component
public class ScheduledTask {

    @Resource
    private WeixinService weixinService;
    /**
     * 一个小时刷新一次第三方access_token
     */
    private static final String REFRESH_COMPONENT_TOKEN_TASK = "0 0 0/1 * * ?";


    /**
     *
     * @throws IOException
     */
    @Scheduled(cron = REFRESH_COMPONENT_TOKEN_TASK)
    public void downloadOrderTaskAM(){
        weixinService.refreshComponentAccessToken();
    }

}