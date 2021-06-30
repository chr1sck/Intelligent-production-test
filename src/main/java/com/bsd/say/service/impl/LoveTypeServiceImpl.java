package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveType;
import com.bsd.say.mapper.LoveLetterMapper;
import com.bsd.say.mapper.LoveTypeMapper;
import com.bsd.say.service.LoveTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("loveTypeService")
@Transactional
public class LoveTypeServiceImpl extends BaseServiceImpl<LoveTypeMapper, LoveType> implements LoveTypeService {
    @Autowired
    protected LoveTypeMapper loveTypeMapper;

    @Override
    public LoveTypeMapper getBaseMapper() {
        return this.loveTypeMapper;
    }

    @Override
    public AjaxResult getLoveTypeList() {
        AjaxResult ajaxResult = new AjaxResult();
        List<LoveType> loveTypes = loveTypeMapper.selectList(Wrappers.<LoveType>lambdaQuery().eq(LoveType::getState,1)
                .orderByAsc(LoveType::getId));
        if (loveTypes.size()==0||loveTypes == null){
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("NOT FOUND");
        }else {
            ajaxResult.setRetcode(AjaxResult.SUCCESS);
            ajaxResult.setData(loveTypes);
        }
        return ajaxResult;
    }

    @Override
    public AjaxResult getLoveTypeById(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("DATA MISSING");
        }else {
            Integer id = data.getInteger("id");
            if (id == null){
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("ID MISSING");
            }else {
                LoveType loveType = loveTypeMapper.selectById(id);
                if (loveType == null){
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("RESULT MISSING");
                }else {
                    ajaxResult.setData(loveType);
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                }
            }
        }
        return ajaxResult;
    }
}
