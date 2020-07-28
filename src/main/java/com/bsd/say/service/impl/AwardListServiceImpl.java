package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.AwardList;
import com.bsd.say.mapper.AwardListMapper;
import com.bsd.say.service.AwardListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service("awardListService")
@Transactional
public class AwardListServiceImpl extends BaseServiceImpl<AwardListMapper, AwardList> implements AwardListService {
    @Value("${award.rule}")
    private Integer rule;

    @Autowired
    protected AwardListMapper awardListMapper;

    @Override
    public AwardListMapper getBaseMapper() {
        return this.awardListMapper;
    }

    /**
     * 抽奖
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult award(AjaxRequest ajaxRequest) {
        synchronized(this){
            AjaxResult ajaxResult = new AjaxResult();
            JSONObject data = ajaxRequest.getData();
            if (data == null){
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("DATA MISSING");
                return ajaxResult;
            }else {
                Integer userId = data.getInteger("userId");
                if (userId == null){
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("USERID MISSING");
                    return ajaxResult;
                }else {
                    AwardList maxIdAward = awardListMapper.selectByMaxId();
                    Integer newAwardNumner = maxIdAward.getAwardNumber()+ 1;
                    AwardList awardList = new AwardList();
                    awardList.setUserId(userId);
                    awardList.setAwardNumber(newAwardNumner);
                    awardList.setCreateDateTime(new Date());
                    awardList.setUpdateDateTime(new Date());
                    //中大奖
                    if (newAwardNumner % rule == 0){
                        awardList.setAwardName("波司登羽绒服");
                        awardList.setAwardType(1);
                        ajaxResult.setRetmsg("恭喜中一等奖，羽绒服");
                    }else {
                        awardList.setAwardName("波司登优惠券");
                        awardList.setAwardType(2);
                        ajaxResult.setRetmsg("恭喜中二等奖，优惠券");
                    }
                    awardListMapper.insert(awardList);
                }
            }
            ajaxResult.setRetcode(AjaxResult.SUCCESS);
            return ajaxResult;
        }
    }
}
