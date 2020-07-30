package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Coupon;
import com.bsd.say.entities.Users;
import com.bsd.say.mapper.CouponMapper;
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
    protected CouponMapper couponMapper;
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
            String code = data.getString("code");
            String receiverName = data.getString("receiverName");
            if (StringUtils.isEmpty(phone)||StringUtils.isEmpty(code)){
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("PHONE OR CODE MISSING");
                return ajaxResult;
            }else {
                if (code.equals(redisTemplate.opsForValue().get(phone))){
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
                            Users users = usersMapper.selectOne(Wrappers.<Users>lambdaQuery().eq(Users::getPhone,phone)
                                    .and(queryWrapper1 -> queryWrapper1.eq(Users::getState,1)));
                            coupon.setUserId(users.getId());
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
                    ajaxResult.setRetmsg("ERROR CODE");
                }
                return ajaxResult;
            }
        }
    }
}
