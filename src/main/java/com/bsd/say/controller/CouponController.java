package com.bsd.say.controller;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Coupon;
import com.bsd.say.service.CouponService;
import com.bsd.say.service.UsersService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("coupon")
@CrossOrigin
public class CouponController  extends BaseController<CouponService, Coupon>{
    @Resource
    private CouponService couponService;

    @Override
    public CouponService getBaseService() {
        return super.getBaseService();
    }

    /**
     * 领取优惠券
     */
    @RequestMapping(value = "/receive-coupon")
    @ResponseBody
    public AjaxResult receiveCoupon(@RequestBody AjaxRequest ajaxRequest){
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = couponService.receiveCoupon(ajaxRequest);
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = e.getMessage() != null ? e.getMessage() : "操作失败";
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg(errMsg);
        }
        return ajaxResult;
    }
}
