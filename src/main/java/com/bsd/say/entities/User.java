package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("user")
@Data
public class User extends BaseEntity{
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
}
