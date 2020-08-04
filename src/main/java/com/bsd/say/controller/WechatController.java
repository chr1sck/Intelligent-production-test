package com.bsd.say.controller;

import com.bsd.say.util.LogUtils;
import com.bsd.say.util.ResponseUtil;
import com.bsd.say.util.Xml2MapUtil;
import com.bsd.say.util.wechat.AesException;
import com.bsd.say.util.wechat.WXBizMsgCrypt;
import com.bsd.say.util.wechat.WeChatUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("wechat")
public class WechatController {
    @Value("${wechat.aesKey}")
    private String aesKey;
    @Value("${wechat.componentToken}")
    private String componentToken;
    @Value("${wechat.appId}")
    private String appId;
    @Resource
    private RedisTemplate redisTemplate;
    Logger logger = LogUtils.getBussinessLogger();
    /**
     * 接收component_verify_ticket 或 authorized事件
     */
    @RequestMapping(value = "/getComponentVerifyTicket")
    @ResponseBody
    public String getComponentVerifyTicket(@RequestParam("timestamp")String timestamp, @RequestParam("nonce")String nonce,
                                           @RequestParam("msg_signature")String msgSignature, @RequestBody String postData) throws IOException {

//        logger.info("接收component_verify_ticket 或 authorized事件");
//        String nonce = request.getParameter("nonce");
//        String timestamp = request.getParameter("timestamp");
//        String msgSignature = request.getParameter("msg_signature");

//        StringBuilder sb = new StringBuilder();
//        BufferedReader in = request.getReader();
//        String line;
//        while((line = in.readLine()) != null) {
//            sb.append(line);
//        }
        System.out.println("nonce: " + nonce);
        System.out.println("timestamp: " + timestamp);
        System.out.println("msgSignature: " + msgSignature);
        System.out.println("postData: " + postData);
        try {
            //这个类是微信官网提供的解密类,需要用到消息校验Token 消息加密Key和服务平台appid
            WXBizMsgCrypt pc = new WXBizMsgCrypt(componentToken,
                    aesKey, appId);
            String xml = pc.decryptMsg(msgSignature, timestamp, nonce, postData);
            Map<String, Object> result = Xml2MapUtil.xml2map(xml);// 将xml转为map

            String componentVerifyTicket = MapUtils.getString(result, "ComponentVerifyTicket");
            // 存储平台授权票据,保存ticket
            String TICKET = componentVerifyTicket;
            redisTemplate.opsForValue().set("component_verify_ticket",TICKET);
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return "success";
    }



    @RequestMapping(value="/{appid}/callback",method={RequestMethod.GET,RequestMethod.POST})
    public void callBackEvent(@PathVariable String appid,
                              HttpServletResponse response,HttpServletRequest request){
        try {
            logger.info(appid+"进入callback+++++++++++++++++++++++++++++++++");
//            handleMessage(request,response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
