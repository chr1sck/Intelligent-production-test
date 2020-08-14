package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Record;
import com.bsd.say.entities.Source;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.mapper.SourceMapper;
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
    @Resource
    private SourceMapper sourceMapper;

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
            logger.info("ajaxRequestData:"+data.toString());
            String openId = data.getString("openId");
//            String code = data.getString("code");
            String qrCode = data.getString("qrCode");
            String postCode = data.getString("postCode");
            String sourceName;
            if (StringUtils.isNotEmpty(qrCode)){
                //二维码编码
                Source source = sourceMapper.selectOne(Wrappers.<Source>lambdaQuery().eq(Source::getQrCode,qrCode)
                        .and(queryWrapper1 -> queryWrapper1.eq(Source::getState,1)));
                if (source == null){
                    logger.info("错误的qrCode来源:"+qrCode);
                    ajaxResult.setRetmsg("未找到二维码code的来源");
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    return ajaxResult;
                }else {
                    sourceName = source.getSourceName();
                }
            }else if (StringUtils.isNotEmpty(postCode)){
                //海报编码
                Source source = sourceMapper.selectOne(Wrappers.<Source>lambdaQuery().eq(Source::getPostCode,postCode)
                        .and(queryWrapper1 -> queryWrapper1.eq(Source::getState,1)));
                if (source == null){
                    logger.info("错误的postCode来源:"+postCode);
                    ajaxResult.setRetmsg("未找到海报code的来源");
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    return ajaxResult;
                }else {
                    sourceName = source.getSourceName();
                }
            }else {
                sourceName = "";
            }
            if (StringUtils.isNotEmpty(openId)){
                //微信端
                JSONObject userInfo = weixinService.getUserInfoByOpenId(openId);
                logger.info("userInfo:" + userInfo.toString());
                String unionId = userInfo.getString("unionid");
                logger.info("union_id:" + unionId);
                Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getOpenId, openId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Record::getState, 1)));
                //微信端新用户第一次访问
                logger.info("open_id:" + openId);
                record.setSource(sourceName);
                record.setUnionId(unionId);
//                    String accessToken = weixin.getString("access_token");
//                    String userInfoUrl = getWxUserInfoUrl + accessToken + "&openid=" + openId + "&lang=zh_CN" ;
//                    String userString = HttpRequestUtils.sendGet(userInfoUrl);
//                    JSONObject userJson = JSONObject.parseObject(userString);
                String nickName = userInfo.getString("nickname");
                record.setNickName(nickName);
                record.setUpdateDateTime(new Date());
                recordMapper.updateById(record);
            } else {
                logger.info("非微信端访问");
                //非微信端待确认
            }
        }
        ajaxResult.setRetmsg("SUCCESS");
        ajaxResult.setRetcode(AjaxResult.SUCCESS);
        return ajaxResult;
    }
}
