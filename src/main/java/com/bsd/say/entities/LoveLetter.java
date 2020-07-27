package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("love_letter")
@Data
public class LoveLetter extends BaseEntity{
    /**
     * 用户id(谁写的爱意情书)
     */
    private String userId;
    /**
     * 情书内容(有字数限制)
     */
    private String content;
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
}
