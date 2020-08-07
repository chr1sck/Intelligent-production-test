package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.*;
import com.bsd.say.mapper.*;
import com.bsd.say.service.AwardListService;
import com.bsd.say.service.CouponService;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.LogUtils;
import com.bsd.say.util.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service("couponService")
@Transactional
public class CouponServiceImpl extends BaseServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Value("${bsd.addCouponUrl}")
    private String addCouponUrl;
    @Value("${bsd.tokenkey}")
    private String tokenkey;
    @Resource
    private UsersMapper usersMapper;
    @Autowired
    private CouponMapper couponMapper;
    @Resource
    private LoveLetterMapper loveLetterMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RecordMapper recordMapper;
    @Autowired
    private AwardListMapper awardListMapper;
    @Override
    public CouponMapper getBaseMapper() {
        return this.couponMapper;
    }
    @Autowired
    WeixinService weixinService;

    private Logger logger = LogUtils.getBussinessLogger();

    /**
     * 领取优惠券
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult receiveCoupon(AjaxRequest ajaxRequest) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        }else {
            String phone = data.getString("phone");
            String noteCode = data.getString("noteCode");
            String receiverName = data.getString("receiverName");
            Boolean isAward = data.getBoolean("isAward");
            String code = data.getString("code");
            if (isAward){
                //如果是抽奖领券,微信code为必填
                if (StringUtils.isBlank(code)){
                    ajaxResult.setRetmsg("CODE MISSING");
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    return ajaxResult;
                }
            }
            if (StringUtils.isEmpty(phone)||StringUtils.isEmpty(noteCode)){
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("PHONE OR CODE MISSING");
                return ajaxResult;
            }else {
                //先校验直接领券的
                if (!isAward){
                    Users users;
                    if (StringUtils.isBlank(code)){
                        //来源H5
                        users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getPhone,phone)
                                .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                        if (users == null){
                            logger.info("非微信端新会员");
                            Users newUsers = new Users();
                            newUsers.setPhone(phone);
                            newUsers.setUserType(1);
                            newUsers.setCreateDateTime(new Date());
                            newUsers.setUpdateDateTime(new Date());
                            usersMapper.insert(newUsers);
                        }else {
                            logger.info("非微信访问端老会员,可能之前用微信访问过");
                            List<Coupon> coupons = couponMapper.selectList(Wrappers.<Coupon>lambdaQuery().eq(Coupon::getUserId,users.getId())
                                    .and(queryWrapper1 -> queryWrapper1.eq(Coupon::getState,1)));
                            if (coupons.size() > 0){
                                ajaxResult.setRetmsg("已经领过");
                                ajaxResult.setRetcode(AjaxResult.FAILED);
                                ajaxResult.setData(false);
                                return ajaxResult;
                            }
                        }
                    }else {
                        //来源微信
                        String unionId = weixinService.getUnionId(code);
                        logger.info("union_id:"+unionId);
                        users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUnionId,unionId)
                                .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                        //防非法请求,再校验一遍
                        List<Coupon> coupons = couponMapper.selectList(Wrappers.<Coupon>lambdaQuery().eq(Coupon::getUserId,users.getId())
                                .and(queryWrapper1 -> queryWrapper1.eq(Coupon::getState,1)));
                        if (coupons.size() > 0){
                            ajaxResult.setRetmsg("非法请求，已经领过");
                            ajaxResult.setRetcode(AjaxResult.FAILED);
                            ajaxResult.setData(false);
                            return ajaxResult;
                        }
                    }
                }


                if (noteCode.equals(redisTemplate.opsForValue().get(phone))){
                    String token = MD5Utils.md5(tokenkey+df.format(new Date()));
                    //验证成功，领券
                    JSONObject request = new JSONObject();
                    request.put("mobileNo",phone);
                    request.put("couponsGroupNo","Q00001326");
                    String url = addCouponUrl + token;
                    try {
                        String result = HttpRequestUtils.sendPost(url,request);
                        JSONObject resultJson = JSONObject.parseObject(result);
                        if (resultJson.getBoolean("success")){
                            Coupon coupon = new Coupon();
                            Users users;
                            if (isAward){
                                String unionId = weixinService.getUnionId(code);
                                logger.info("union_id:"+unionId);
                                users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUnionId,unionId)
                                        .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                                users.setPhone(phone);
                                users.setUpdateDateTime(new Date());
                                usersMapper.updateById(users);

                                AwardList awardList = awardListMapper.selectOne(Wrappers.<AwardList>lambdaQuery().eq(AwardList::getUserId,users.getId())
                                        .and(queryWrapper1 -> queryWrapper1.eq(AwardList::getState,1)));
                                awardList.setIsReceive(1);
                                awardList.setUpdateDateTime(new Date());
                                awardListMapper.updateById(awardList);
                                //统计领取二等奖
                                Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getUnionId,unionId)
                                        .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
                                record.setIsHavaCoupon2("有");
                                record.setUpdateDateTime(new Date());
                                recordMapper.updateById(record);
                            }
                            else {
                                if (StringUtils.isBlank(code)){
                                    //来源H5
                                    users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getPhone,phone)
                                            .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                                    Record record = new Record();
                                    record.setPhone(phone);
                                    record.setIsHavaCoupon1("有");
                                    record.setCreateDateTime(new Date());
                                    recordMapper.insert(record);
                                }else {
                                    //来源微信
                                    String unionId = weixinService.getUnionId(code);
                                    users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getPhone,phone)
                                            .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                                    if (users.getUserType() == 2){
                                        //既是寄件人又是收信人
                                        users.setUserType(3);
                                        usersMapper.updateById(users);
                                    }else if (users.getUserType() == 0){
                                        users.setUserType(1);
                                        usersMapper.updateById(users);
                                    }

                                    Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getUnionId,unionId)
                                            .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
                                    record.setIsHavaCoupon1("有");
                                    record.setUpdateDateTime(new Date());
                                    recordMapper.updateById(record);
                                }
                                coupon.setUserId(users.getId());
                                coupon.setCreateDateTime(new Date());
                                coupon.setUpdateDateTime(new Date());
                                coupon.setReceiverName(receiverName);
                                couponMapper.insert(coupon);
                                ajaxResult.setRetmsg("SUCCESS");
                                ajaxResult.setRetcode(AjaxResult.SUCCESS);
                            }
                        }else {
                            ajaxResult.setRetcode(AjaxResult.FAILED);
                            ajaxResult.setRetmsg(resultJson.getString("errorMessage"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("ERROR NOTECODE");
                }
                return ajaxResult;
            }
        }
    }

    /**
     * 有没有领取过优惠券(微信端)
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult isValidGetCoupon(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        }else {
//            String phone = data.getString("phone");
            String code = data.getString("code");
            if (StringUtils.isEmpty(code)){
                ajaxResult.setRetmsg("code MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            }
//            Boolean isWechat = true;
            String unionId = weixinService.getUnionId(code);
            logger.info("union_id:"+unionId);
            Users users= usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUnionId,unionId)
                    .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
            if (users == null){
                ajaxResult.setRetmsg("可以领取优惠券");
                ajaxResult.setRetcode(AjaxResult.SUCCESS);
                ajaxResult.setData(true);
            }else {
                List<Coupon> coupons = couponMapper.selectList(Wrappers.<Coupon>lambdaQuery().eq(Coupon::getUserId,users.getId())
                        .and(queryWrapper1 -> queryWrapper1.eq(Coupon::getState,1)));
                if (coupons.size() == 0 || coupons == null){
                    ajaxResult.setRetmsg("可以领取优惠券");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(true);
                }else {
                    ajaxResult.setRetmsg("已经领过");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(false);
                }
            }
        }
        return ajaxResult;
    }
}
