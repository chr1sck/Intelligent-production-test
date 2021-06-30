package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("award_list")
@Data
public class AwardList extends BaseEntity{
    /**
     * 中奖用户id
     */
    private Long userId;

    /**
     * 中奖类型(1-一等奖  2-二等奖)
     */
    private int awardType;

    /**
     * 奖品名
     */
    private String awardName;

    /**
     * 奖品地址
     */
    private String awardUrl;

    /**
     * 抽奖排名
     */
    private Integer awardNumber;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 收奖人
     */
    private String receiverName;

    /**
     * 是否已被领取
     */
    private int isReceive;
}
