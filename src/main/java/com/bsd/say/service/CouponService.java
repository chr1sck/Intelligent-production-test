package com.bsd.say.service;

import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Coupon;

public interface CouponService extends BaseService<Coupon> {
    AjaxResult receiveCoupon(AjaxRequest ajaxRequest);

    AjaxResult isReceiveCoupon(AjaxRequest ajaxRequest);
}
