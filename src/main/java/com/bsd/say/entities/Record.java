package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 数据记录
 */
@TableName("record")
@Data
public class Record extends BaseEntity{

    private Long id;

    private String source;

    private String openId;

    private String unionId;

    private String nickName;

    private String name;

    private String phone;

    private String fan;

    private int createLetterTimes;

    private int receiveLetterTimes;

    private String isHavaCoupon1;//是否有直接领取的优惠券

    private String isUseCoupon1;//是否使用直接领取的优惠券

    private String isHavaCoupon2;//是否有二等奖优惠券

    private String isUseCoupon2;//二等奖优惠券是否使用

    private String isAward;//是否中一等奖

    private String awardName;//领一等奖人名

    private String awardPhone;//领一等奖手机号

    private String address;//领取一等奖地址

}
