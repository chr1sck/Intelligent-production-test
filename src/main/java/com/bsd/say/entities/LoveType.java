package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("love_type")
@Data
public class LoveType extends BaseEntity{
    /**
     * 绑定情书id
     */
    private Integer letterId;
    /**
     * 爱意类型内容
     */
    private String content;
}
