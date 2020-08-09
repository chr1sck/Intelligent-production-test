package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.LoveLetter;
import com.bsd.say.entities.Record;
import com.bsd.say.entities.Source;
import com.bsd.say.entities.Users;
import com.bsd.say.mapper.LoveLetterMapper;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.mapper.SourceMapper;
import com.bsd.say.mapper.UsersMapper;
import com.bsd.say.service.LoveLetterService;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.LogUtils;
import com.bsd.say.util.RandomUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service("loveLetterService")
@Transactional
public class LoveLetterServiceImpl extends BaseServiceImpl<LoveLetterMapper, LoveLetter> implements LoveLetterService {
    @Value("${wechat.getWxUserInfoUrl}")
    private String getWxUserInfoUrl;
    @Autowired
    protected LoveLetterMapper loveLetterMapper;
    @Autowired
    private RecordMapper recordMapper;
    @Autowired
    private WeixinService weixinService;
    @Autowired
    private UsersMapper usersMapper;
    @Autowired
    private SourceMapper sourceMapper;
    @Override
    public LoveLetterMapper getBaseMapper() {
        return this.loveLetterMapper;
    }
    private Logger logger = LogUtils.getBussinessLogger();
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
            String code = data.getString("code");
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
                loveLetter.setContent(content);
                loveLetter.setSenderName(sender_name);
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
            if (StringUtils.isNotEmpty(code)){
                //来源于微信
                String unionId = weixinService.getUnionId(code);
                logger.info("union_id:"+unionId);
                Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getUnionId,unionId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
                int createLetterTimes = record.getCreateLetterTimes();
                record.setCreateLetterTimes(createLetterTimes + 1);
                record.setUpdateDateTime(new Date());
                recordMapper.updateById(record);
            }else {
                //非微信端来源，待确认
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
            String code = data.getString("code");
            String letterId = data.getString("letterId");
            String qrCode = data.getString("qrCode");
            String postCode = data.getString("postCode");
            String sourceName;
            if (StringUtils.isNotEmpty(qrCode)){
                //二维码编码
                Source source = sourceMapper.selectOne(Wrappers.<Source>lambdaQuery().eq(Source::getQrCode,qrCode)
                        .and(queryWrapper1 -> queryWrapper1.eq(Source::getState,1)));
                if (source == null){
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
                    ajaxResult.setRetmsg("未找到海报code的来源");
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    return ajaxResult;
                }else {
                    sourceName = source.getSourceName();
                }
            }else {
                sourceName = "";
            }

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
            if (StringUtils.isNotEmpty(code)){
                //来源于微信
                String unionId = weixinService.getUnionId(code);
                logger.info("union_id:"+unionId);
                Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getUnionId,unionId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
                if (record == null){
                    //新用户第一次收到情书礼物
                    JSONObject weixin = weixinService.getAccessToken(code);
                    String openId = weixin.getString("openid");
                    String accessToken = weixin.getString("access_token");
                    String userInfoUrl = getWxUserInfoUrl + accessToken + "&openid=" + openId + "&lang=zh_CN" ;
                    String userString = HttpRequestUtils.sendGet(userInfoUrl);
                    JSONObject userJson = JSONObject.parseObject(userString);
                    String nickName = userJson.getString("nickname");
                    Record record1 = new Record();
                    record1.setSource("微信");
                    record1.setUnionId(unionId);
                    record1.setOpenId(openId);
                    record1.setNickName(nickName);
                    record1.setCreateDateTime(new Date());
                    record1.setUpdateDateTime(new Date());
                    record1.setReceiveLetterTimes(1);
                    record1.setSource(sourceName);
                    recordMapper.insert(record1);
                }else {
                    int receiveLetterTimes = record.getReceiveLetterTimes();
                    record.setReceiveLetterTimes(receiveLetterTimes + 1);
                    record.setUpdateDateTime(new Date());
                    recordMapper.updateById(record);
                }
            }else {
                //非微信端待确认。。。

            }
        }
        return ajaxResult;
    }
}
