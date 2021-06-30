package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("coupon")
@Data
public class Coupon extends BaseEntity{
    /**
     * 波司登返回的优惠券号
     */
    private String couponNo;

    /**
     * 优惠券券面值
     */
    private Double couponValue;

    /**
     * 绑定的userId
     */
    private Long userId;

    /**
     * 领券人名
     */
    private String receiverName;
}
