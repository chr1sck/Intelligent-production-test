package com.bsd.say.service.impl;

import com.bsd.say.entities.Coupon;
import com.bsd.say.mapper.CouponMapper;
import com.bsd.say.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("couponService")
@Transactional
public class CouponServiceImpl extends BaseServiceImpl<CouponMapper, Coupon> implements CouponService {
    @Autowired
    protected CouponMapper couponMapper;

    @Override
    public CouponMapper getBaseMapper() {
        return this.couponMapper;
    }
}
