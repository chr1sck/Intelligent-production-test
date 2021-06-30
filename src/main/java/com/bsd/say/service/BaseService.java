package com.bsd.say.service;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bsd.say.entities.BaseEntity;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Validated
public interface BaseService<T> extends IService<T> {

    boolean saveOrUpdate(@Valid BaseEntity entity, Long operatorId);

    IPage<T> page(JSONObject data);
}