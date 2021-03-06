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
     * ηζζδΉ¦
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
            String openId = data.getString("openId");
//            String code = data.getString("code");
            if (StringUtils.isBlank(content)){
                ajaxResult.setRetmsg("CONTENT MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            }else if(StringUtils.isBlank(sender_name)){
                ajaxResult.setRetmsg("SENDER MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            } else {
                //ιζΊ6δ½+13δ½ζΆι΄ζ³δ½δΈΊletterId
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
            if (StringUtils.isNotEmpty(openId)){
                //ζ₯ζΊδΊεΎ?δΏ‘

                JSONObject userInfo = weixinService.getUserInfoByOpenId(openId);
                String unionId = userInfo.getString("unionid");
//                String unionId = weixinService.getUnionId(code);
                logger.info("union_id:"+unionId);
                Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getOpenId,openId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
                int createLetterTimes = record.getCreateLetterTimes();
                record.setCreateLetterTimes(createLetterTimes + 1);
                record.setUpdateDateTime(new Date());
                recordMapper.updateById(record);
            }else {
                //ιεΎ?δΏ‘η«―ζ₯ζΊοΌεΎη‘?θ?€
            }
        }
        ajaxResult.setRetcode(AjaxResult.SUCCESS);
        ajaxResult.setRetmsg("SUCCESS");
        return ajaxResult;
    }

    /**
     * ζ₯ηζδΉ¦
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
            String openId = data.getString("openId");
//            String code = data.getString("code");
            String letterId = data.getString("letterId");
            String qrCode = data.getString("qrCode");
            String postCode = data.getString("postCode");
            String sourceName;
            if (StringUtils.isNotEmpty(qrCode)){
                //δΊη»΄η ηΌη 
                Source source = sourceMapper.selectOne(Wrappers.<Source>lambdaQuery().eq(Source::getQrCode,qrCode)
                        .and(queryWrapper1 -> queryWrapper1.eq(Source::getState,1)));
                if (source == null){
                    logger.info("ιθ――ηqrCodeζ₯ζΊ:"+qrCode);
                    ajaxResult.setRetmsg("ζͺζΎε°δΊη»΄η codeηζ₯ζΊ");
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    return ajaxResult;
                }else {
                    sourceName = source.getSourceName();
                }
            }else if (StringUtils.isNotEmpty(postCode)){
                //ζ΅·ζ₯ηΌη 
                Source source = sourceMapper.selectOne(Wrappers.<Source>lambdaQuery().eq(Source::getPostCode,postCode)
                        .and(queryWrapper1 -> queryWrapper1.eq(Source::getState,1)));
                if (source == null){
                    logger.info("ιθ――ηpostCodeζ₯ζΊ:"+postCode);
                    ajaxResult.setRetmsg("ζͺζΎε°ζ΅·ζ₯codeηζ₯ζΊ");
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
                    int readTimes = loveLetter.getReadTimes();
                    loveLetter.setReadTimes(readTimes + 1);
                    loveLetter.setUpdateDateTime(new Date());
                    loveLetterMapper.updateById(loveLetter);
                    ajaxResult.setRetmsg("SUCCESS");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(loveLetter);
                }
            }
            if (StringUtils.isNotEmpty(openId)){
                //ζ₯ζΊδΊεΎ?δΏ‘
                JSONObject userInfo = weixinService.getUserInfoByOpenId(openId);
                String unionId = userInfo.getString("unionid");
//                String unionId = weixinService.getUnionId(code);
                logger.info("union_id:"+unionId);
                Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getOpenId,openId)
                        .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
                if (record == null){
                    //ζ°η¨ζ·η¬¬δΈζ¬‘ζΆε°ζδΉ¦η€Όη©
//                    JSONObject weixin = weixinService.getAccessToken(code);
//                    String openId = weixin.getString("openid");
//                    String accessToken = weixin.getString("access_token");
//                    String userInfoUrl = getWxUserInfoUrl + accessToken + "&openid=" + openId + "&lang=zh_CN" ;
//                    String userString = HttpRequestUtils.sendGet(userInfoUrl);
//                    JSONObject userJson = JSONObject.parseObject(userString);
                    String nickName = userInfo.getString("nickname");
                    Record record1 = new Record();
                    record1.setSource(sourceName);
                    record1.setUnionId(unionId);
                    record1.setOpenId(openId);
                    record1.setNickName(nickName);
                    record1.setCreateDateTime(new Date());
                    record1.setUpdateDateTime(new Date());
                    record1.setReceiveLetterTimes(1);
                    record1.setSource(sourceName);
                    recordMapper.insert(record1);
                }else {
                    Integer receiveLetterTimes = record.getReceiveLetterTimes();
                    record.setReceiveLetterTimes(receiveLetterTimes + 1);
                    record.setUpdateDateTime(new Date());
                    recordMapper.updateById(record);
                }
            }else {
                //ιεΎ?δΏ‘η«―εΎη‘?θ?€γγγ
            }
        }
        return ajaxResult;
    }
}
