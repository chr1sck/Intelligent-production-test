//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.bsd.say.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bsd.say.constants.Constants;
import com.bsd.say.constants.FillterConstants;
import com.bsd.say.entities.BaseEntity;
import com.bsd.say.service.BaseService;
import com.bsd.say.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional
@Service("baseService")
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> implements BaseService<T> {

    @Autowired
    private M baseMapper;

    public M getBaseMapper() {
        return this.baseMapper;
    }

    public boolean saveOrUpdate(BaseEntity entity, Long operatorId) {

        Date date = DateUtils.date();
        Long id = entity.getId();
        if (id == null) {

            entity.setCreateBy(operatorId);
            entity.setCreateDateTime(date);
            entity.setState(Constants.ENABLE);
        }
        entity.setUpdateBy(operatorId);
        entity.setUpdateDateTime(date);
        return super.saveOrUpdate((T) entity);
    }


    public IPage<T> page(JSONObject data) {


        //TODO: 注入用户信息
        Subject subject = SecurityUtils.getSubject();
        System.out.println("subject" + subject);

        long current = data.getLong("current");
        long size = data.getLong("pageSize");
        String sort = data.getString("sort");
        String sortField = data.getString("sortField");
        JSONArray filters = data.getJSONArray("filter");
        IPage<T> page = new Page<>(current, size);
        QueryWrapper<T> wrapper = new QueryWrapper<>();

        if (StringUtils.isNotEmpty(sort)) {

            String direction = data.getString("direction");
            if (direction == null) direction = "DESC";
            if (direction.equals("DESC")) {

                wrapper.orderByDesc(sortField);
            } else {
                wrapper.orderByAsc(sortField);
            }
        }
        if (filters != null) {

            for (int i = 0; i < filters.size(); i++) {

                JSONObject filter = filters.getJSONObject(i);
                // 查询关键字不能为空
                String key = filter.getString("key");
                if (key == null || key.isEmpty()) continue;
                // 查询模式
                String action = filter.getString("action");
                // 判断查询类型
                String type = filter.getString("type");

                if (type == null || type.isEmpty()) {

                    addFilter(wrapper, key, action, filter.getString("value"));
                } else if (type.equals("integer") || type.equals("int")) {
                    addFilter(wrapper, key, action, filter.getInteger("value"));
                } else if (type.equals("array")) {
                    addFilter(wrapper, key, action, filter.getJSONArray("value"));
                } else if (type.equals("rangeStr")) {
                    JSONObject value = filter.getJSONObject("value");
                    wrapper.ge(key, value.getString("start")).le(key, value.getString("end"));
                } else if (type.equals("rangeInt")) {
                    JSONObject value = filter.getJSONObject("value");
                    wrapper.ge(key, value.getInteger("start")).le(key, value.getInteger("end"));
                } else if (type.equals("in")) {
                    JSONArray value = filter.getJSONArray("value");
                    wrapper.in(key, value);
                }
            }
        }


        return page(page, wrapper);
    }


    private void addFilter(QueryWrapper queryWrapper, String key, String action, Object value) {

        if (action == null || action.equals(FillterConstants.EQUAL) || action.equals(FillterConstants.EQUALSYM)) {

            queryWrapper.eq(key, value);
        } else if (action.equals(FillterConstants.LIKE) || action.equals(FillterConstants.LIKESYM)) {

            queryWrapper.like(key, value);
        } else if (action.equals(FillterConstants.IN)) {

            queryWrapper.in(key, value);
        } else if (action.equals(FillterConstants.GTESYM) || action.equals(FillterConstants.GTE)) {

            queryWrapper.ge(key, value);
        } else if (action.equals(FillterConstants.LTESYM) || action.equals(FillterConstants.LTE)) {

            queryWrapper.le(key, value);
        }
    }


}