package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("source")
@Data
public class Source extends BaseEntity{

    private String sourceName;

    private String url;

    private String qrCode;

    private String postCode;

}
