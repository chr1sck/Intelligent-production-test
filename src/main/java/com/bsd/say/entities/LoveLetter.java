package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("love_letter")
@Data
public class LoveLetter extends BaseEntity{
    /**
     * 用户id(谁写的爱意情书)
     */
    private String openId;

    /**
     * 情书类型
     */
    private Integer loveType;
    /**
     * 被送人手机号
     */
    private String receivePhone;
    /**
     * 接收人姓名
     */
    private String receiveName;
    /**
     * 落款人
     */
    private String senderName;
    /**
     * 渠道来源
     */
    private String source;
    /**
     * 信自定义内容
     */
    private String content;

    /**
     * 信的唯一Id
     */
    private String letterId;

    /**
     * 读信数
     */
    private int readTimes;
}
