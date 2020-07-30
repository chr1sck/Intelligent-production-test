package com.bsd.say.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bsd.say.beans.AjaxRequest;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Users;
import com.bsd.say.mapper.UsersMapper;
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
public class UsersServiceImpl extends BaseServiceImpl<UsersMapper,Users> implements UsersService {
    @Resource
    private RedisTemplate redisTemplate;
    @Value("${bsd.tokenkey}")
    private String tokenkey;
    @Value("${bsd.sendSource}")
    private String sendSource;
    @Value("${bsd.verifySMSCodeUrl}")
    private String verifySMSCodeUrl;
    @Autowired
    protected UsersMapper usersMapper;

    @Override
    public UsersMapper getBaseMapper() {
        return this.usersMapper;
    }

    /**
     * 发送验证码
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult sendNote(AjaxRequest ajaxRequest) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        AjaxResult ajaxResult = new AjaxResult();
        JSONObject data = ajaxRequest.getData();
        if (data == null){
            ajaxResult.setRetmsg("DATA MISSING");
            ajaxResult.setRetcode(AjaxResult.FAILED);
            return ajaxResult;
        }else {
            String phone = data.getString("phone");
            if (StringUtils.isEmpty(phone)){
                ajaxResult.setRetcode(AjaxResult.FAILED);
                ajaxResult.setRetmsg("PHONE MISSING");
                return ajaxResult;
            }else {
                int radomInt = new Random().nextInt(999999);
                String noteCode = String.valueOf(radomInt);
                String token = MD5Utils.md5(tokenkey + df.format(new Date()));
                String param = "&mobileNo=" + phone + "&verifyCode=" + noteCode + "&sendSource=" + sendSource;
                String result = HttpRequestUtils.sendGet(verifySMSCodeUrl+token+param);
                JSONObject resultJson = JSONObject.parseObject(result);
                if (resultJson.getBoolean("success")){
                    redisTemplate.opsForValue().set(phone,noteCode,60, TimeUnit.SECONDS);
                    ajaxResult.setRetmsg("SUCCESS");
                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
                    ajaxResult.setData(noteCode);
                    return ajaxResult;
                }else {
                    ajaxResult.setRetcode(AjaxResult.FAILED);
                    ajaxResult.setRetmsg("SEND ERROR");
                    return ajaxResult;
                }
            }
        }
    }

    /**
     * 校验验证码
     * @param ajaxRequest
     * @return
     */
    @Override
    public AjaxResult confirmNote(AjaxRequest ajaxRequest) {
//        AjaxResult ajaxResult = new AjaxResult();
//        JSONObject data = ajaxRequest.getData();
//        if (data == null){
//            ajaxResult.setRetmsg("DATA MISSING");
//            ajaxResult.setRetcode(AjaxResult.FAILED);
//            return ajaxResult;
//        }else {
//            String phone = data.getString("phone");
//            String code = data.getString("code");
//            if (StringUtils.isEmpty(phone)||StringUtils.isEmpty(code)){
//                ajaxResult.setRetcode(AjaxResult.FAILED);
//                ajaxResult.setRetmsg("PHONE OR CODE MISSING");
//                return ajaxResult;
//            }else {
//                if (code.equals(redisTemplate.opsForValue().get(phone))){
//                    ajaxResult.setRetcode(AjaxResult.SUCCESS);
//                    ajaxResult.setRetmsg("SUCCESS");
//                }else {
//                    ajaxResult.setRetcode(AjaxResult.FAILED);
//                    ajaxResult.setRetmsg("ERROR CODE");
//                }
//                return ajaxResult;
//            }
//        }
        return null;
    }


}
