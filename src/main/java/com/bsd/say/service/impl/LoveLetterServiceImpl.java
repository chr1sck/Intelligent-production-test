package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveLetter;
import com.bsd.say.mapper.LoveLetterMapper;
import com.bsd.say.service.LoveLetterService;
import com.bsd.say.util.RandomUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service("loveLetterService")
@Transactional
public class LoveLetterServiceImpl extends BaseServiceImpl<LoveLetterMapper, LoveLetter> implements LoveLetterService {
    @Autowired
    protected LoveLetterMapper loveLetterMapper;

    @Override
    public LoveLetterMapper getBaseMapper() {
        return this.loveLetterMapper;
    }

    /**
     * 生成情书
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult createLoveLetter(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        }else{
            String content = data.getString("content");
            String sender_name = data.getString("sender_name");
            Integer love_type = data.getInteger("love_type");
            String receive_name = data.getString("receive_name");
            if (StringUtils.isBlank(content)){
                ajaxResult.setRetmsg("CONTENT MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            }else if(StringUtils.isBlank(sender_name)){
                ajaxResult.setRetmsg("SENDER MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            } else {
                //随机6位+13位时间戳作为letterId
                LoveLetter loveLetter = new LoveLetter();
                String letterId = RandomUtils.random(6) + String.valueOf(new Date().getTime());
                loveLetter.setLetterId(letterId);
                if (love_type!=null){
                    loveLetter.setLoveType(love_type);
                }
                if (StringUtils.isNotEmpty(receive_name)){
                    loveLetter.setReceiveName(receive_name);
                }
                loveLetter.setCreateDateTime(new Date());
                loveLetter.setUpdateDateTime(new Date());
                loveLetterMapper.insert(loveLetter);
                ajaxResult.setData(letterId);
            }
        }
        ajaxResult.setRetcode(AjaxResult.SUCCESS);
        ajaxResult.setRetmsg("SUCCESS");
        return ajaxResult;
    }

    /**
     * 查看情书
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult getLoveLetter(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        }else{
            String letterId = data.getString("letterId");
            if (StringUtils.isBlank(letterId)){
                ajaxResult.setRetmsg("LETTERID MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            }else {
                LoveLetter loveLetter = loveLetterMapper.selectOne(Wrappers.<LoveLetter>lambdaQuery().eq(LoveLetter::getLetterId,letterId)
                        .and(queryWrapper1 -> queryWrapper1.eq(LoveLetter::getState,1)));
                if (loveLetter == null){
                    ajaxResult.setRetmsg("NOT FOUND");
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    return ajaxResult;
                }else {
                    ajaxResult.setRetmsg("SUCCESS");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(loveLetter);
                }
            }
        }
        return ajaxResult;
    }
}
