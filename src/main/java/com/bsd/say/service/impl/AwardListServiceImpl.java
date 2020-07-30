package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.AwardList;
import com.bsd.say.entities.Users;
import com.bsd.say.mapper.AwardListMapper;
import com.bsd.say.mapper.UsersMapper;
import com.bsd.say.service.AwardListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("awardListService")
@Transactional
public class AwardListServiceImpl extends BaseServiceImpl<AwardListMapper, AwardList> implements AwardListService {
    @Value("${award.rule}")
    private Integer rule;
    @Resource
    private UsersMapper usersMapper;
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
                String code = data.getString("code");
                if (StringUtils.isEmpty(code)){
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("CODE MISSING");
                    return ajaxResult;
                } else {
                    String unionId = "123";
                    Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUnionId,unionId)
                            .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                    AwardList maxIdAward = awardListMapper.selectByMaxId();
                    Integer newAwardNumner = maxIdAward.getAwardNumber()+ 1;
                    AwardList awardList = new AwardList();
                    awardList.setUserId(users.getId());
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
                    ajaxResult.setData(awardList);
                }
            }
            ajaxResult.setRetcode(AjaxResult.SUCCESS);
            return ajaxResult;
        }
    }

    /**
     * 判断有没有抽过奖品 微信code必传
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult isAward(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("DATA MISSING");
            return ajaxResult;
        }else {
            String code = data.getString("code");
            if (StringUtils.isEmpty(code)){
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("CODE MISSING");
                return ajaxResult;
            } else {
                String unionId = "123";
                Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUnionId,unionId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                if (users == null){
                    //新会员直接创，肯定没抽过奖
                    Users newUsers = new Users();
                    newUsers.setUnionId(unionId);
                    newUsers.setUserType(2);
                    newUsers.setCreateDateTime(new Date());
                    newUsers.setUpdateDateTime(new Date());
                    usersMapper.insert(newUsers);
                    ajaxResult.setRetmsg("可以抽奖");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(true);
                }else {
                    if (users.getUserType() == 1){
                        //既是寄件人又是收信人
                        users.setUserType(3);
                        usersMapper.updateById(users);
                    }else if (users.getUserType() == 0){
                        users.setUserType(2);
                        usersMapper.updateById(users);
                    }
                    List<AwardList> awardList = awardListMapper.selectList(Wrappers.<AwardList>lambdaQuery()
                            .eq(AwardList::getUserId,users.getId()).and(queryWrapper1 -> queryWrapper1
                                    .eq(AwardList::getState,1)));
                    if (awardList.size() == 0 || awardList == null){
                        ajaxResult.setRetmsg("可以抽奖");
                        ajaxResult.setRetcode(AjaxResult.SUCCESS);
                        ajaxResult.setData(true);
                    }else {
                        ajaxResult.setRetmsg("已经抽过了");
                        ajaxResult.setRetcode(AjaxResult.SUCCESS);
                        ajaxResult.setData(false);
                    }
                }
            }
        }
        return ajaxResult;
    }
}
