package com.bsd.say.config;

import com.bsd.say.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class InitConfig {

    @Value("${award.amount}")
    private Integer amount;
    @Resource
    RedisService redisService;

    @PostConstruct
    public void initAwardConfig() {


        for (int i = 0; i < amount; i++) {

            redisService.set("award-" + i, i);
        }
    }


}
