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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
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
    public void getComponentVerifyTicket(HttpServletRequest request, HttpServletResponse response) throws IOException, AesException, DocumentException {

        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String signature = request.getParameter("signature");
        String msgSignature = request.getParameter("msg_signature");
//        String postData = request.getParameter("postData");
        System.out.println("nonce: " + nonce);
        System.out.println("timestamp: " + timestamp);
        System.out.println("msgSignature: " + msgSignature);
        StringBuilder sb = new StringBuilder();
        BufferedReader in = request.getReader();
        String line;
        while((line = in.readLine()) != null) {
            sb.append(line);
        }
        String postData = sb.toString();
        System.out.println("postData: " + postData);
        try {
            //这个类是微信官网提供的解密类,需要用到消息校验Token 消息加密Key和服务平台appid
            WXBizMsgCrypt pc = new WXBizMsgCrypt(componentToken,
                    aesKey, componentAppId);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(postData);
            InputSource is = new InputSource(sr);
            org.w3c.dom.Document document = db.parse(is);

            org.w3c.dom.Element root = document.getDocumentElement();
            NodeList nodelist1 = root.getElementsByTagName("Encrypt");
            String encrypt = nodelist1.item(0).getTextContent();

            String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
            String fromXML = String.format(format, encrypt);
            String xml = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
            Map<String, Object> result = Xml2MapUtil.xml2map(xml);// 将xml转为map

            String componentVerifyTicket = MapUtils.getString(result, "ComponentVerifyTicket");
            // 存储平台授权票据,保存ticket
            String TICKET = componentVerifyTicket;
            redisTemplate.opsForValue().set("component_verify_ticket",TICKET);
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        output(response, "success");
    }

    /**
     * 工具类：回复微信服务器"文本消息"
     * @param response
     * @param returnvaleue
     */
    public void output(HttpServletResponse response,String returnvaleue){
        try {
            PrintWriter pw = response.getWriter();
            pw.write(returnvaleue);
//			System.out.println("****************returnvaleue***************="+returnvaleue);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    /**
//     * 处理授权事件的推送
//     *
//     * @param request
//     * @throws IOException
//     * @throws AesException
//     * @throws DocumentException
//     */
//    public void processAuthorizeEvent(HttpServletRequest request) throws IOException, DocumentException, AesException {
//        String nonce = request.getParameter("nonce");
//        String timestamp = request.getParameter("timestamp");
//        String signature = request.getParameter("signature");
//        String msgSignature = request.getParameter("msg_signature");
//
//        if (!StringUtils.isNotBlank(msgSignature))
//            return;// 微信推送给第三方开放平台的消息一定是加过密的，无消息加密无法解密消息
//        boolean isValid = checkSignature(COMPONENT_TOKEN, signature, timestamp, nonce);
//        if (isValid) {
//            StringBuilder sb = new StringBuilder();
//            BufferedReader in = request.getReader();
//            String line;
//            while ((line = in.readLine()) != null) {
//                sb.append(line);
//            }
//            String xml = sb.toString();
////            LogUtil.info("第三方平台全网发布-----------------------原始 Xml="+xml);
//            String encodingAesKey = COMPONENT_ENCODINGAESKEY;// 第三方平台组件加密密钥
//            String appId = getAuthorizerAppidFromXml(xml);// 此时加密的xml数据中ToUserName是非加密的，解析xml获取即可
//            //LogUtil.info("第三方平台全网发布-------------appid----------getAuthorizerAppidFromXml(xml)-----------appId="+appId);
//            WXBizMsgCrypt pc = new WXBizMsgCrypt(COMPONENT_TOKEN, encodingAesKey, COMPONENT_APPID);
//            xml = pc.decryptMsg(msgSignature, timestamp, nonce, xml);
////            LogUtil.info("第三方平台全网发布-----------------------解密后 Xml="+xml);
//            processAuthorizationEvent(xml);
//        }
//    }
//
//
    @RequestMapping(value="/{appid}/callback",method={RequestMethod.GET,RequestMethod.POST})
    public void callBackEvent(HttpServletRequest request, HttpServletResponse response) throws IOException, DocumentException, AesException {

//        String msgSignature = request.getParameter("msg_signature");
//        //LogUtil.info("第三方平台全网发布-------------{appid}/callback-----------验证开始。。。。msg_signature="+msgSignature);
//        if (!StringUtils.isNotBlank(msgSignature))
//            return;// 微信推送给第三方开放平台的消息一定是加过密的，无消息加密无法解密消息
//
//        StringBuilder sb = new StringBuilder();
//        BufferedReader in = request.getReader();
//        String line;
//        while ((line = in.readLine()) != null) {
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
////           LogUtil.info("全网发布接入检测消息反馈开始---------------APPID="+ APPID +"------------------------toUserName="+toUserName);
//        checkWeixinAllNetworkCheck(request,response,xml);
    }
//
//
//    public void checkWeixinAllNetworkCheck(HttpServletRequest request, HttpServletResponse response,String xml) throws DocumentException, IOException, AesException{
//        String nonce = request.getParameter("nonce");
//        String timestamp = request.getParameter("timestamp");
//        String msgSignature = request.getParameter("msg_signature");
//
//        WXBizMsgCrypt pc = new WXBizMsgCrypt(componentToken, aesKey, componentAppId);
//        xml = pc.decryptMsg(msgSignature, timestamp, nonce, xml);
//
//        Document doc = DocumentHelper.parseText(xml);
//        Element rootElt = doc.getRootElement();
//        String msgType = rootElt.elementText("MsgType");
//        String toUserName = rootElt.elementText("ToUserName");
//        String fromUserName = rootElt.elementText("FromUserName");
//
////        LogUtil.info("---全网发布接入检测--step.1-----------msgType="+msgType+"-----------------toUserName="+toUserName+"-----------------fromUserName="+fromUserName);
////        LogUtil.info("---全网发布接入检测--step.2-----------xml="+xml);
//        if("event".equals(msgType)){
////        	 LogUtil.info("---全网发布接入检测--step.3-----------事件消息--------");
//            String event = rootElt.elementText("Event");
//            replyEventMessage(request,response,event,toUserName,fromUserName);
//        }else if("text".equals(msgType)){
////        	 LogUtil.info("---全网发布接入检测--step.3-----------文本消息--------");
//            String content = rootElt.elementText("Content");
//            processTextMessage(request,response,content,toUserName,fromUserName);
//        }
//    }
//
//    public void replyEventMessage(HttpServletRequest request, HttpServletResponse response, String event, String toUserName, String fromUserName) throws DocumentException, IOException {
//        String content = event + "from_callback";
////        LogUtil.info("---全网发布接入检测------step.4-------事件回复消息  content="+content + "   toUserName="+toUserName+"   fromUserName="+fromUserName);
//        replyTextMessage(request,response,content,toUserName,fromUserName);
//    }
//
//
//    /**
//     * 回复微信服务器"文本消息"
//     * @param request
//     * @param response
//     * @param content
//     * @param toUserName
//     * @param fromUserName
//     * @throws DocumentException
//     * @throws IOException
//     */
//    public void replyTextMessage(HttpServletRequest request, HttpServletResponse response, String content, String toUserName, String fromUserName) throws DocumentException, IOException {
//        Long createTime = Calendar.getInstance().getTimeInMillis() / 1000;
//        StringBuffer sb = new StringBuffer();
//        sb.append("<xml>");
//        sb.append("<ToUserName><![CDATA["+fromUserName+"]]></ToUserName>");
//        sb.append("<FromUserName><![CDATA["+toUserName+"]]></FromUserName>");
//        sb.append("<CreateTime>"+createTime+"</CreateTime>");
//        sb.append("<MsgType><![CDATA[text]]></MsgType>");
//        sb.append("<Content><![CDATA["+content+"]]></Content>");
//        sb.append("</xml>");
//        String replyMsg = sb.toString();
//
//        String returnvaleue = "";
//        try {
//            WXBizMsgCrypt pc = new WXBizMsgCrypt(componentToken, aesKey, componentAppId);
//            returnvaleue = pc.encryptMsg(replyMsg, createTime.toString(), "easemob");
////            System.out.println("------------------加密后的返回内容 returnvaleue： "+returnvaleue);
//        } catch (AesException e) {
//            e.printStackTrace();
//        }
//        output(response, returnvaleue);
//    }
//
//    public void processTextMessage(HttpServletRequest request, HttpServletResponse response,String content,String toUserName, String fromUserName) throws IOException, DocumentException{
//        if("TESTCOMPONENT_MSG_TYPE_TEXT".equals(content)){
//            String returnContent = content+"_callback";
//            replyTextMessage(request,response,returnContent,toUserName,fromUserName);
//        }else if(StringUtils.startsWithIgnoreCase(content, "QUERY_AUTH_CODE")){
//            output(response, "");
//            //接下来客服API再回复一次消息
//            replyApiTextMessage(request,response,content.split(":")[1],fromUserName);
//        }
//    }
//
//    public void replyApiTextMessage(HttpServletRequest request, HttpServletResponse response, String auth_code, String fromUserName) throws DocumentException, IOException {
//        String authorization_code = auth_code;
//        // 得到微信授权成功的消息后，应该立刻进行处理！！相关信息只会在首次授权的时候推送过来
//        System.out.println("------step.1----使用客服消息接口回复粉丝----逻辑开始-------------------------");
//        try {
//            ApiComponentToken apiComponentToken = new ApiComponentToken();
//            apiComponentToken.setComponent_appid(COMPONENT_APPID);
//            apiComponentToken.setComponent_appsecret(COMPONENT_APPSECRET);
//            WeixinOpenAccountEntity  entity = getWeixinOpenAccount(APPID);
//            apiComponentToken.setComponent_verify_ticket(entity.getTicket());
//            String component_access_token = JwThirdAPI.getAccessToken(apiComponentToken);
//
//            System.out.println("------step.2----使用客服消息接口回复粉丝------- component_access_token = "+component_access_token + "---------authorization_code = "+authorization_code);
//            net.sf.json.JSONObject authorizationInfoJson = JwThirdAPI.getApiQueryAuthInfo(COMPONENT_APPID, authorization_code, component_access_token);
//            System.out.println("------step.3----使用客服消息接口回复粉丝-------------- 获取authorizationInfoJson = "+authorizationInfoJson);
//            net.sf.json.JSONObject infoJson = authorizationInfoJson.getJSONObject("authorization_info");
//            String authorizer_access_token = infoJson.getString("authorizer_access_token");
//
//
//            Map<String,Object> obj = new HashMap<String,Object>();
//            Map<String,Object> msgMap = new HashMap<String,Object>();
//            String msg = auth_code + "_from_api";
//            msgMap.put("content", msg);
//
//            obj.put("touser", fromUserName);
//            obj.put("msgtype", "text");
//            obj.put("text", msgMap);
//            JwThirdAPI.sendMessage(obj, authorizer_access_token);
//        } catch (WexinReqException e) {
//            e.printStackTrace();
//        }
//
//    }
}
