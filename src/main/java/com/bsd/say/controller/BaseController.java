package com.bsd.say.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.BaseEntity;
import com.bsd.say.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class BaseController<B extends BaseService, T extends BaseEntity> {

    @Autowired
    private B baseService;

    private Class<T> entityClass;

    public B getBaseService() {
        return this.baseService;
    }

    public BaseController() {

        Type type = getClass().getGenericSuperclass();
        ParameterizedType ptype = (ParameterizedType) type;
        Type[] types = ptype.getActualTypeArguments();
        entityClass = (Class<T>) types[1];
    }

    public AjaxResult saveOrUpdate(AjaxRequest ajaxRequest) {

        JSONObject data = ajaxRequest.getData();
        T entityClass = data.toJavaObject(this.entityClass);
        baseService.saveOrUpdate(entityClass, 12L);
        return new AjaxResult();
    }


    public AjaxResult page(AjaxRequest ajaxRequest) {

        AjaxResult ajaxResult = new AjaxResult();
        JSONObject jsonObject = ajaxRequest.getData();
        IPage<T> page = baseService.page(jsonObject);
        ajaxResult.setData(page);
        return ajaxResult;
    }
}