package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Record;
import com.bsd.say.entities.Users;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.mapper.UsersMapper;
import com.bsd.say.service.RedisService;
import com.bsd.say.service.UsersService;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service("usersService")
@Transactional
public class UsersServiceImpl extends BaseServiceImpl<UsersMapper, Users> implements UsersService {


    @Resource
    private RedisTemplate redisTemplate;
    @Value("${bsd.tokenkey}")
    private String tokenkey;
    @Value("${bsd.sendSource}")
    private String sendSource;
    @Value("${bsd.verifySMSCodeUrl}")
    private String verifySMSCodeUrl;
    @Value("${wechat.getWxUserInfoUrl}")
    private String getWxUserInfoUrl;
    @Autowired
    protected UsersMapper usersMapper;
    @Autowired
    private WeixinService weixinService;
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private RedisService redisService;
    static final String RECORD_PREFIX = "BSD_RECORD_";


    @Override
    public UsersMapper getBaseMapper() {
        return this.usersMapper;
    }

    /**
     * 发送验证码
     *
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult sendNote(AjaxRequest ajaxRequest) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null) {
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        } else {
            String phone = data.getString("phone");
            if (StringUtils.isEmpty(phone)) {
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("PHONE MISSING");
                return ajaxResult;
            } else {
                int radomInt = new Random().nextInt(999999);
                String noteCode = String.valueOf(radomInt);
                String token = MD5Utils.md5(tokenkey + df.format(new Date()));
                String param = "&mobileNo=" + phone + "&verifyCode=" + noteCode + "&sendSource=" + sendSource;
                String result = HttpRequestUtils.sendGet(verifySMSCodeUrl + token + param);
                JSONObject resultJson = JSONObject.parseObject(result);
                if (resultJson.getBoolean("success")) {
                    redisTemplate.opsForValue().set(phone, noteCode, 60, TimeUnit.SECONDS);
                    ajaxResult.setRetmsg("SUCCESS");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(noteCode);
                    return ajaxResult;
                } else {
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("SEND ERROR");
                    return ajaxResult;
                }
            }
        }
    }

    @Override
    public AjaxResult isSubscribe(AjaxRequest ajaxRequest) {
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null) {
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        } else {
            String code = data.getString("code");
            if (StringUtils.isEmpty(code)) {
                ajaxResult.setRetmsg("CODE MISSING");
                ajaxResult.setRetcode(AjaxResult.FAILED);
                return ajaxResult;
            } else {
                JSONObject weixin = weixinService.getAccessToken(code);
                String openId = weixin.getString("openid");
                String accessToken = weixin.getString("access_token");
                String userInfoUrl = getWxUserInfoUrl + accessToken + "&openid=" + openId + "&lang=zh_CN";
                JSONObject userinfo = JSONObject.parseObject(HttpRequestUtils.sendGet(userInfoUrl));
                int subscribe = userinfo.getInteger("subscribe");
                if (subscribe == 1) {
                    ajaxResult.setData(1);
                    ajaxResult.setRetmsg("已关注公众号");
                } else {
                    ajaxResult.setData(0);
                    ajaxResult.setRetmsg("未关注公众号");
                }
                return ajaxResult;
            }
        }
    }

    /**
     * 通过openId获取record中的name 和 phone
     *
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult getUserInfoByOpenId(AjaxRequest ajaxRequest) {

        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null) {
            ajaxResult.setRetcode(AjaxResult.FAILED);
            ajaxResult.setRetmsg("data missing");
            return ajaxResult;
        } else {
            String openId = data.getString("openId");
            if (redisService.exists(RECORD_PREFIX + openId)) {

                String userInfoStr = redisService.get(RECORD_PREFIX + openId).toString();
                ajaxResult.setData(JSONObject.parseObject(userInfoStr));
                ajaxResult.setRetcode(AjaxResult.SUCCESS);
            } else {

                if (StringUtils.isBlank(openId)) {
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("openId missing");
                    return ajaxResult;
                } else {
                    JSONObject userInfo = new JSONObject();
                    Record record = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getOpenId, openId)
                            .and(queryWrapper1 -> queryWrapper1.eq(Record::getState, 1)));
                    if (record != null) {
                        userInfo.put("name", record.getName());
                        userInfo.put("phone", record.getPhone());
                    }
                    redisService.set(RECORD_PREFIX + openId, userInfo.toJSONString());
                    ajaxResult.setData(userInfo);
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                }
            }
        }
        return ajaxResult;
    }

}
