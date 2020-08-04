package com.bsd.say.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.LogUtils;
import com.bsd.say.util.ResponseUtil;
import com.bsd.say.util.Xml2MapUtil;
import com.bsd.say.util.wechat.AesException;
import com.bsd.say.util.wechat.WXBizMsgCrypt;
import com.bsd.say.util.wechat.WeChatUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
import java.util.Calendar;
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
    @Value("${wechat.componentAppId}")
    private String componentAppId;
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
                    aesKey, componentAppId);
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
    public void callBackEvent(HttpServletRequest request,@PathVariable("APPID") String appid,
                              HttpServletResponse response) throws IOException, DocumentException {
//        String msgSignature = request.getParameter("msg_signature");
//        logger.info("第三方平台全网发布-------------{appid}/callback-----------验证开始。。。。msg_signature=" + msgSignature);
//        if (!StringUtils.isNotBlank(msgSignature)) {
//            return;// 微信推送给第三方开放平台的消息一定是加过密的，无消息加密无法解密消息
//        }
//        StringBuilder sb =new StringBuilder();
//        BufferedReader in = request.getReader();
//        String line;
//        while ((line = in.readLine()) !=null) {
//            sb.append(line);
//        }
//        in.close();
//
//        String xml = sb.toString();
//        Document doc = DocumentHelper.parseText(xml);
//        Element rootElt = doc.getRootElement();
//        String toUserName = rootElt.elementText("ToUserName");
//
//        //微信全网测试账号
////        if (StringUtils.equalsIgnoreCase(toUserName, APPID)) {
//        logger.info("全网发布接入检测消息反馈开始---------------APPID=" + appid +"------------------------toUserName=" + toUserName);
//        checkWeixinAllNetworkCheck(request, response, xml);
    }

    public void checkWeixinAllNetworkCheck(HttpServletRequest request, HttpServletResponse response,String xml, String appid) throws DocumentException, IOException, AesException{
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String msgSignature = request.getParameter("msg_signature");

        WXBizMsgCrypt pc = new WXBizMsgCrypt(componentToken, aesKey, appId);
        xml = pc.decryptMsg(msgSignature, timestamp, nonce, xml);

        Document doc = DocumentHelper.parseText(xml);
        Element rootElt = doc.getRootElement();
        String msgType = rootElt.elementText("MsgType");
        String toUserName = rootElt.elementText("ToUserName");
        String fromUserName = rootElt.elementText("FromUserName");

        System.out.println("---全网发布接入检测--step.1-----------msgType="+msgType+"-----------------toUserName="+toUserName+"-----------------fromUserName="+fromUserName);
//        LogUtil.info("---全网发布接入检测--step.2-----------xml="+xml);
        if("event".equals(msgType)){
//           LogUtil.info("---全网发布接入检测--step.3-----------事件消息--------");
            String event = rootElt.elementText("Event");
            replyEventMessage(request,response,event,toUserName,fromUserName,appid);
        }else if("text".equals(msgType)){
//           LogUtil.info("---全网发布接入检测--step.3-----------文本消息--------");
            String content = rootElt.elementText("Content");
            processTextMessage(request,response,content,toUserName,fromUserName,appid);
        }
    }

    public void replyEventMessage(HttpServletRequest request, HttpServletResponse response, String event, String toUserName, String fromUserName, String appid) throws DocumentException, IOException {
        String content = event + "from_callback";
//        LogUtil.info("---全网发布接入检测------step.4-------事件回复消息  content="+content + "   toUserName="+toUserName+"   fromUserName="+fromUserName);
        replyTextMessage(request,response,content,toUserName,fromUserName, appid);
    }

    public void processTextMessage(HttpServletRequest request, HttpServletResponse response,String content,String toUserName, String fromUserName, String appid) throws IOException, DocumentException{
        if("TESTCOMPONENT_MSG_TYPE_TEXT".equals(content)){
            String returnContent = content+"_callback";
            replyTextMessage(request,response,returnContent,toUserName,fromUserName,appid);
        }else if(StringUtils.startsWithIgnoreCase(content, "QUERY_AUTH_CODE")){
            output(response, "");
            //接下来客服API再回复一次消息
            replyApiTextMessage(request,response,content.split(":")[1],fromUserName,appid);
        }
    }

    /**
     * 回复微信服务器"文本消息"
     * @param request
     * @param response
     * @param content
     * @param toUserName
     * @param fromUserName
     * @throws DocumentException
     * @throws IOException
     */
    public void replyTextMessage(HttpServletRequest request, HttpServletResponse response, String content, String toUserName, String fromUserName, String appid) throws DocumentException, IOException {
        Long createTime = Calendar.getInstance().getTimeInMillis() / 1000;
        StringBuffer sb = new StringBuffer();
        sb.append("<xml>");
        sb.append("<ToUserName><![CDATA["+fromUserName+"]]></ToUserName>");
        sb.append("<FromUserName><![CDATA["+toUserName+"]]></FromUserName>");
        sb.append("<CreateTime>"+createTime+"</CreateTime>");
        sb.append("<MsgType><![CDATA[text]]></MsgType>");
        sb.append("<Content><![CDATA["+content+"]]></Content>");
        sb.append("</xml>");
        String replyMsg = sb.toString();

        String returnvaleue = "";
        try {
            WXBizMsgCrypt pc = new WXBizMsgCrypt(componentToken, aesKey, appId);
            returnvaleue = pc.encryptMsg(replyMsg, createTime.toString(), "easemob");
//            System.out.println("------------------加密后的返回内容 returnvaleue： "+returnvaleue);
        } catch (AesException e) {
            e.printStackTrace();
        }
        output(response, returnvaleue);
    }

    /**
     *
     * @param response
     * @param returnvaleue
     */
    public void output(HttpServletResponse response, String returnvaleue) {
        try {
            PrintWriter pw = response.getWriter();
            pw.write(returnvaleue);
            // System.out.println("****************returnvaleue***************="+returnvaleue);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void replyApiTextMessage(HttpServletRequest request, HttpServletResponse response, String auth_code, String fromUserName, String appid) throws DocumentException, IOException {
        // 得到微信授权成功的消息后，应该立刻进行处理！！相关信息只会在首次授权的时候推送过来
//        System.out.println("------step.1----使用客服消息接口回复粉丝----逻辑开始-------------------------");
//        try {
//            System.out.println("------step.1----使用客服消息接口回复粉丝----逻辑开始-------------------------auth_code: "+auth_code+"  thirdWeixinService.getComponent_access_token:"+redisTemplate.opsForValue().get("component_access_token").toString());
//            String url = "https://api.weixin.qq.com/cgi-bin/component/api_query_auth?component_access_token="+ redisTemplate.opsForValue().get("component_access_token").toString();
//            JSONObject jsonObject1 = new JSONObject();
//            jsonObject1.put("component_appid", appId);
//            jsonObject1.put("authorization_code", auth_code);
//            JSONObject jsonRes = JSONObject.parseObject(HttpRequestUtils.sendPost(url,jsonObject1));
//            System.out.println("------step.1----使用客服消息接口回复粉丝----逻辑开始---------------------jsonRes:"+jsonRes.toString());
//
//            System.out.println("------step.1----使用客服消息接口回复粉丝----逻辑开始---------------------jsonRes.authorization_info:"+jsonRes.get("authorization_info"));
//            ThirdWeixin thirdWeixin = new ThirdWeixin();
//            thirdWeixin = JSON.parseObject(JSON.toJSONString(jsonRes.get("authorization_info")), ThirdWeixin.class);
//            thirdWeixin.setEntCode("test");
//            CommonUtil.setInsertCommonField(thirdWeixin, "system_getauthinfo");
//            thirdWeixinService.saveThirdWeixin(thirdWeixin);
//
//
//            String msg = auth_code + "_from_api";
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("touser", fromUserName);
//            jsonObject.put("msgtype", "text");
//            JSONObject text = new JSONObject();
//            text.put("content", msg);
//            jsonObject.put("text", text);
//            WeixinToKenServiceImpl.httpRequest("https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="+thirdWeixin.getAuthorizer_access_token(), "POST", jsonObject);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
