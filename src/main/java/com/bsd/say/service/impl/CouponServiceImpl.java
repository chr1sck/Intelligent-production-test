package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Coupon;
import com.bsd.say.entities.LoveLetter;
import com.bsd.say.entities.Users;
import com.bsd.say.mapper.CouponMapper;
import com.bsd.say.mapper.LoveLetterMapper;
import com.bsd.say.mapper.UsersMapper;
import com.bsd.say.service.CouponService;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Override
    public CouponMapper getBaseMapper() {
        return this.couponMapper;
    }

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
                                String unionId = "123";
                                users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getUnionId,unionId)
                                        .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                                users.setPhone(phone);
                                usersMapper.insert(users);
                            }else {
                                users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getPhone,phone)
                                        .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                            }
                            coupon.setUserId(users.getId());
                            coupon.setCreateDateTime(new Date());
                            coupon.setUpdateDateTime(new Date());
                            coupon.setReceiverName(receiverName);
                            couponMapper.insert(coupon);
                            ajaxResult.setRetmsg("SUCCESS");
                            ajaxResult.setRetcode(AjaxResult.SUCCESS);
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
     * 有没有领取过优惠券  领取优惠券手机号必传
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
            String phone = data.getString("phone");
            if (StringUtils.isBlank(phone)){
                ajaxResult.setRetmsg("PHONE MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            }
            Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getPhone,phone)
                    .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
            if (users == null){
                //新会员直接创，肯定没领取过券
                Users newUsers = new Users();
                newUsers.setPhone(phone);
                newUsers.setUserType(1);
                newUsers.setCreateDateTime(new Date());
                newUsers.setUpdateDateTime(new Date());
                usersMapper.insert(newUsers);
                ajaxResult.setRetmsg("可以领取优惠券");
                ajaxResult.setRetcode(AjaxResult.SUCCESS);
                ajaxResult.setData(true);
            }else {
                //老会员
                if (users.getUserType() == 2){
                    //既是寄件人又是收信人
                    users.setUserType(3);
                    usersMapper.updateById(users);
                }else if (users.getUserType() == 0){
                    users.setUserType(1);
                    usersMapper.updateById(users);
                }
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
