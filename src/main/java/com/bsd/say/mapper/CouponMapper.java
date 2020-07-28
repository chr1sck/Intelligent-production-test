package com.bsd.say.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bsd.say.entities.Coupon;
import org.springframework.stereotype.Repository;

@Repository("couponMapper")
public interface CouponMapper extends BaseMapper<Coupon> {
}
