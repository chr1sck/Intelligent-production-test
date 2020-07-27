package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("award_list")
@Data
public class AwardList extends BaseEntity{
    /**
     * 中奖用户id
     */
    private Integer userId;

    /**
     * 中奖类型(1-一等奖  2-二等奖)
     */
    private int awardType;

    /**
     * 奖品名
     */
    private String awardName;

    /**
     * 省
     */
    private String province;

    /**
     * 市
     */
    private String city;

    /**
     * 区
     */
    private String area;

    /**
     * 详细地藏
     */
    private String address;
}
