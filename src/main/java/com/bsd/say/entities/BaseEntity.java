package com.bsd.say.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * 基础信息
 */
@Data
public class BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createDateTime;
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateDateTime;
    private Long createBy;
    private Long updateBy;
    private int state;
}
