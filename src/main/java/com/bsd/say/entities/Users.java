package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("users")
@Data
public class Users extends BaseEntity{
    /**
     * 用户手机号
     */
    private String phone;
    /**
     * 用户openId
     */
    private String openId;
    /**
     * 0-白嫖客 1-寄信人 2-被送信人 3-既是寄信也是被送
     */
    private int userType;
    /**
     * 用户unionId （微信来源）
     */
    private String unionId;
}
