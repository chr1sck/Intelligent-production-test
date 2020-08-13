package com.bsd.say.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.entities.AwardList;
import com.bsd.say.mapper.AwardListMapper;
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
    @Resource
    private AwardListMapper awardListMapper;

    @PostConstruct
    public void initAwardConfig() {


        int awardCount = awardListMapper.selectCount(Wrappers.<AwardList>lambdaQuery().eq(AwardList::getAwardType, 1));
        System.out.println("----award---" + awardCount);
        for (int i = awardCount + 1; i < amount + 1; i++) {

            redisService.set("award-" + i, i);
        }
    }


}
