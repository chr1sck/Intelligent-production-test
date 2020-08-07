package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Record;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.mapper.UsersMapper;
import com.bsd.say.service.RecordService;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service("recordService")
@Transactional
public class RecordServiceImpl extends BaseServiceImpl<RecordMapper,Record> implements RecordService {
    @Value("${wechat.getWxUserInfoUrl}")
    private String getWxUserInfoUrl;
    @Resource
    private RecordMapper recordMapper;

    private Logger logger = LogUtils.getBussinessLogger();

    @Resource
    WeixinService weixinService;

    @Override
    public RecordMapper getBaseMapper() {
        return this.recordMapper;
    }


    /**
     * 访问首页的时候，去创建这个记录
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult createRecord(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        }
        else {
            String code = data.getString("code");
            if (StringUtils.isNotEmpty(code)){
                //微信端
                String unionId = weixinService.getUnionId(code);
                logger.info("union_id:"+unionId);
                Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getUnionId,unionId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
                if (record == null){
                    //微信端新用户第一次访问
                    JSONObject weixin = weixinService.getAccessToken(code);
                    String openId = weixin.getString("openid");
                    logger.info("open_id:"+openId);
                    Record newRecord = new Record();
                    newRecord.setOpenId(openId);
                    newRecord.setUnionId(unionId);
                    String accessToken = weixin.getString("access_token");
                    String userInfoUrl = getWxUserInfoUrl + accessToken + "&openid=" + openId + "&lang=zh_CN" ;
                    String userString = HttpRequestUtils.sendGet(userInfoUrl);
                    JSONObject userJson = JSONObject.parseObject(userString);
                    String nickName = userJson.getString("nickname");
                    newRecord.setNickName(nickName);
                    newRecord.setCreateDateTime(new Date());
                    newRecord.setUpdateDateTime(new Date());
                    newRecord.setSource("微信");
                    recordMapper.insert(newRecord);
                }else {
                    //不是第一次访问
                    logger.info("unionId" + unionId);
                    /**
                     * 更新用户信息之类的
                     */
                }
            }else {
                //非微信端待确认
            }
        }
        ajaxResult.setRetmsg("SUCCESS");
        ajaxResult.setRetcode(AjaxResult.SUCCESS);
        return ajaxResult;
    }
}
