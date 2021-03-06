package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.AwardList;
import com.bsd.say.entities.Coupon;
import com.bsd.say.entities.Record;
import com.bsd.say.entities.Users;
import com.bsd.say.exception.AreadyAwardException;
import com.bsd.say.mapper.AwardListMapper;
import com.bsd.say.mapper.CouponMapper;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.mapper.UsersMapper;
import com.bsd.say.service.AwardListService;
import com.bsd.say.service.RedisService;
import com.bsd.say.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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
    @Value("${award.amount}")
    private Integer amount;
    @Resource
    private UsersMapper usersMapper;
    @Autowired
    protected AwardListMapper awardListMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedisService redisService;
    @Resource
    private CouponMapper couponMapper;
    @Resource
    private RecordMapper recordMapper;
    @Autowired
    WeixinService weixinService;

    @Override
    public AwardListMapper getBaseMapper() {
        return this.awardListMapper;
    }

    private Logger logger = LogUtils.getBussinessLogger();

    /**
     * 抽奖
     *
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult award(AjaxRequest ajaxRequest) throws AreadyAwardException {


        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null) {
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("DATA MISSING");
            return ajaxResult;
        } else {
            String openId = data.getString("openId");
//                String code = data.getString("code");
            if (StringUtils.isEmpty(openId)) {
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("openId MISSING");
                return ajaxResult;
            } else {
                Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getOpenId, openId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Users::getState, 1)));

                if (redisService.exists("user-award-" + users.getId())) {

                    AwardList userAWardList = userAWardList = awardListMapper.selectOne(Wrappers.<AwardList>lambdaQuery().eq(AwardList::getUserId, users.getId()));
                    if (null != userAWardList) {


                        redisService.set("user-award-" + users.getId(), userAWardList.toString());
                        throw new AreadyAwardException("您已经抽过奖了， 不要太贪心哦~~~");
                    }
                }

//                synchronized (this) {
                    AwardList maxIdAward = awardListMapper.selectByMaxId();
                    Integer newAwardNumner = maxIdAward.getAwardNumber() + 1;
                    AwardList awardList = new AwardList();
                    awardList.setUserId(users.getId());
                    awardList.setAwardNumber(newAwardNumner);
                    awardList.setCreateDateTime(new Date());
                    awardList.setUpdateDateTime(new Date());

                    int awardNumber = newAwardNumner / rule;
                    //中大奖
                    if (newAwardNumner % rule == 0) {

                        if (newAwardNumner > rule * amount && !redisService.exists("award-" + (awardNumber))) {

                            logger.info("没一等奖了");
                            awardList.setAwardName("波司登优惠券");
                            awardList.setAwardType(2);
                            ajaxResult.setRetmsg("恭喜中二等奖，优惠券");
                        } else {
                            awardList.setAwardName("波司登羽绒服");
                            awardList.setAwardType(1);
                            ajaxResult.setRetmsg("恭喜中一等奖，羽绒服");
                            redisService.remove("award-" + awardNumber);
                        }
                    } else {
                        awardList.setAwardName("波司登优惠券");
                        awardList.setAwardType(2);
                        ajaxResult.setRetmsg("恭喜中二等奖，优惠券");
                    }

                    try{

                        awardListMapper.insert(awardList);

                    }catch (Exception e){
                        ajaxResult.setRetmsg("恭喜中二等奖，优惠券");
                    }
                    redisService.set("user-award-" + users.getId(), awardList.toString());
                    ajaxResult.setData(awardList);
                }

//            }
        }
        ajaxResult.setRetcode(AjaxResult.SUCCESS);
        return ajaxResult;
    }

    /**
     * 判断有没有抽过奖品 微信code必传
     *
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult isValidLottery(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null) {
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("DATA MISSING");
            return ajaxResult;
        } else {
//            String code = data.getString("code");
            String openId = data.getString("openId");
            if (StringUtils.isEmpty(openId)) {
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("openId MISSING");
                return ajaxResult;
            } else {

                Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getOpenId, openId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Users::getState, 1)));
                if (users == null) {
                    //新会员直接创，肯定没抽过奖
                    Users newUsers = new Users();
                    newUsers.setOpenId(openId);
                    newUsers.setUserType(2);
                    newUsers.setCreateDateTime(new Date());
                    newUsers.setUpdateDateTime(new Date());
                    usersMapper.insert(newUsers);
                    ajaxResult.setRetmsg("可以抽奖");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(true);
                } else {
                    if (users.getUserType() == 1) {
                        //既是寄件人又是收信人
                        users.setUserType(3);
                        usersMapper.updateById(users);
                    } else if (users.getUserType() == 0) {
                        users.setUserType(2);
                        usersMapper.updateById(users);
                    }
                    List<AwardList> awardList = awardListMapper.selectList(Wrappers.<AwardList>lambdaQuery()
                            .eq(AwardList::getUserId, users.getId()).and(queryWrapper1 -> queryWrapper1
                                    .eq(AwardList::getState, 1)));
                    if (awardList.size() == 0 || awardList == null) {
                        ajaxResult.setRetmsg("可以抽奖");
                        ajaxResult.setRetcode(AjaxResult.SUCCESS);
                        ajaxResult.setData(true);
                    } else {
                        ajaxResult.setRetmsg("已经抽过了");
                        ajaxResult.setRetcode(AjaxResult.SUCCESS);
                        ajaxResult.setData(false);
                    }
                }
            }
        }
        return ajaxResult;
    }

    /**
     * 一等奖填写地址信息 (校验验证码)
     *
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult saveAward(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null) {
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("DATA MISSING");
            return ajaxResult;
        } else {
//            String code = data.getString("code");
            String openId = data.getString("openId");
            String phone = data.getString("phone");
            String noteCode = data.getString("noteCode");
            String address = data.getString("address");
            String receiverName = data.getString("receiverName");
            if (StringUtils.isBlank(openId) || StringUtils.isBlank(phone)
                    || StringUtils.isBlank(noteCode) || StringUtils.isBlank(receiverName)) {
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("PARAM MISSING");
                return ajaxResult;
            } else {
                if (noteCode.equals(redisTemplate.opsForValue().get(phone))) {
                    //验证成功
                    JSONObject userInfo = weixinService.getUserInfoByOpenId(openId);
                    String unionId = userInfo.getString("unionid");
//                    String unionId = weixinService.getUnionId(openId);
                    logger.info("union_id:" + unionId);
                    Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getOpenId, openId)
                            .and(queryWrapper1 -> queryWrapper1.eq(Users::getState, 1)));
                    if (users == null) {
                        ajaxResult.setRetcode(AjaxResult.FAILED);
                        ajaxResult.setRetmsg("NOT FOUND USERS");
                        return ajaxResult;
                    }
                    AwardList awardList = awardListMapper.selectOne(Wrappers.<AwardList>lambdaQuery().eq(AwardList::getUserId, users.getId())
                            .and(queryWrapper1 -> queryWrapper1.eq(AwardList::getState, 1)));
                    if (awardList == null) {
                        ajaxResult.setRetcode(AjaxResult.FAILED);
                        ajaxResult.setRetmsg("NOT FOUND AWARD");
                        return ajaxResult;
                    }
                    if (StringUtils.isBlank(address)) {
                        ajaxResult.setRetcode(AjaxResult.FAILED);
                        ajaxResult.setRetmsg("ADDRESS MISSING");
                        return ajaxResult;
                    }
                    awardList.setAddress(address);
                    awardList.setIsReceive(1);
                    awardList.setPhone(phone);
                    awardList.setReceiverName(receiverName);
                    awardListMapper.updateById(awardList);
                    ajaxResult.setRetmsg("SUCCESS");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);

                    //抽中一等奖，必定微信来源
                    Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getOpenId, openId)
                            .and(queryWrapper1 -> queryWrapper1.eq(Record::getState, 1)));
                    record.setIsAward("中奖");
                    record.setAwardName(receiverName);
                    record.setAddress(address);
                    record.setAwardPhone(phone);
                    recordMapper.updateById(record);
                } else {
                    //短信验证失败
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("验证码错误或已超时，请重新填写");
                }
            }
        }
        return ajaxResult;
    }

    /**
     * 通过code获取个人优惠券和抽奖
     *
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult getAwardList(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null) {
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("DATA MISSING");
            return ajaxResult;
        } else {
            String openId = data.getString("openId");
//            String code = data.getString("code");
            if (StringUtils.isBlank(openId)) {
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("CODE MISSING");
                return ajaxResult;
            } else {
                JSONObject userInfo = weixinService.getUserInfoByOpenId(openId);
                String unionId = userInfo.getString("unionid");
                logger.info("union_id:" + unionId);
                Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getOpenId, openId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Users::getState, 1)));
                if (users == null) {
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    return ajaxResult;
                } else {
                    Coupon coupon = couponMapper.selectOne(Wrappers.<Coupon>lambdaQuery().eq(Coupon::getUserId, users.getId())
                            .and(queryWrapper1 -> queryWrapper1.eq(Coupon::getState, 1)));
                    AwardList awardList = awardListMapper.selectOne(Wrappers.<AwardList>lambdaQuery().eq(AwardList::getUserId, users.getId())
                            .and(queryWrapper1 -> queryWrapper1.eq(AwardList::getState, 1)));
                    JSONObject result = new JSONObject();
                    if (coupon != null) {
                        String jsonString = JSONObject.toJSONString(coupon);
                        JSONObject jsonObject = JSONObject.parseObject(jsonString);
                        result.put("coupon", jsonObject);
                    } else {
                        result.put("coupon", new JSONObject());
                    }
                    if (awardList != null) {
                        String jsonString = JSONObject.toJSONString(awardList);
                        JSONObject jsonObject = JSONObject.parseObject(jsonString);
                        result.put("awardList", jsonObject);
                    } else {
                        result.put("awardList", new JSONObject());
                    }
                    ajaxResult.setRetmsg("SUCCESS");
                    ajaxResult.setData(result);
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                }
            }
        }
        return ajaxResult;
    }

    public static void main(String[] args) {

        System.out.println(1 / 5);
    }

}
