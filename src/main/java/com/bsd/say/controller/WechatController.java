package com.bsd.say.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.bsd.say.beans.AjaxResult;
import com.bsd.say.entities.Record;
import com.bsd.say.mapper.RecordMapper;
import com.bsd.say.service.WxOpenServiceDemo;
import com.bsd.say.service.impl.WeixinService;
import com.bsd.say.util.AESWithJCEUtils;
import com.bsd.say.util.HttpRequestUtils;
import com.bsd.say.util.LogUtils;
import com.bsd.say.util.wechat.AesException;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.open.bean.message.WxOpenXmlMessage;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

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
    @Autowired
    private WxOpenServiceDemo wxOpenService;
    Logger logger = LogUtils.getBussinessLogger();
    @Autowired
    private WeixinService weixinService;
    @Resource
    private RecordMapper recordMapper;

    /**
     * 接收component_verify_ticket 或 authorized事件
     */
    @RequestMapping("/getComponentVerifyTicket")
    public Object receiveTicket(@RequestBody(required = false) String requestBody, @RequestParam("timestamp") String timestamp,
                                @RequestParam("nonce") String nonce, @RequestParam("signature") String signature,
                                @RequestParam(name = "encrypt_type", required = false) String encType,
                                @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        this.logger.info(
                "\n接收微信请求：[signature=[{}], encType=[{}], msgSignature=[{}],"
                        + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ",
                signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (!StringUtils.equalsIgnoreCase("aes", encType)
                || !wxOpenService.getWxOpenComponentService().checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        // aes加密的消息
        WxOpenXmlMessage inMessage = WxOpenXmlMessage.fromEncryptedXml(requestBody,
                wxOpenService.getWxOpenConfigStorage(), timestamp, nonce, msgSignature);
        this.logger.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
        try {
            String out = wxOpenService.getWxOpenComponentService().route(inMessage);
            this.logger.debug("\n组装回复信息：{}", out);
        } catch (WxErrorException e) {
            this.logger.error("receive_ticket", e);
        }


        return "success";
    }

    /**
     * 工具类：回复微信服务器"文本消息"
     *
     * @param response
     * @param returnvaleue
     */
    public void output(HttpServletResponse response, String returnvaleue) {
        try {
            PrintWriter pw = response.getWriter();
            pw.write(returnvaleue);
//			logger.info("****************returnvaleue***************="+returnvaleue);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/{appid}/callback", method = {RequestMethod.GET, RequestMethod.POST})
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

    @RequestMapping("test")
    public void test() {
//        Object o = redisTemplate.opsForValue().getOperations();
        weixinService.refreshComponentAccessToken();
    }

    @RequestMapping("autologin")
    public AjaxResult autoLogin(@RequestParam String openId) throws IOException {

        logger.info("请求openid:"+ openId);
        String result1 = HttpRequestUtils.sendGet("https://api.weq.me/wx/token.php?id=15969759463491&key=1234567890123456");

        JSONObject result2 = JSONObject.parseObject(result1);
        String result3 = result2.getString("access_token");
        String pubkey = "1234567890123456";
        String iv = "WJi7HTZQoh8eHjup";
        String decode = AESWithJCEUtils.aesDecode(result3, pubkey, iv);
        String resutl = HttpRequestUtils.sendGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + decode + "&openid=" + openId + "&lang=zh_CN");
        JSONObject jsonObject = JSONObject.parseObject(resutl);
        Integer subscribe = jsonObject.getInteger("subscribe");
        logger.info("subscribe："+ subscribe);
        Record recordByOpenId = recordMapper.selectOne(Wrappers.<Record>lambdaQuery().eq(Record::getOpenId,openId)
                .and(queryWrapper1 -> queryWrapper1.eq(Record::getState,1)));
        if (subscribe == 0){
            //未关注公众号
            if (recordByOpenId == null){
                logger.info("新粉丝第一次进入");
                Record record = new Record();
                record.setOpenId(openId);
                record.setFan("新粉丝");
                record.setCreateDateTime(new Date());
                recordMapper.insert(record);
            }else {
                logger.info("游客访问过，但未关注");
            }
        }else {
            //关注过公众号
            if (recordByOpenId == null){
                logger.info("老粉丝第一次进入");
                Record record = new Record();
                record.setOpenId(openId);
                record.setFan("老粉丝");
                record.setCreateDateTime(new Date());
                recordMapper.insert(record);
            }else {
                logger.info("老粉丝访问过,已关注");
            }
        }
        AjaxResult ajaxResult =  new AjaxResult();
        ajaxResult.setData(jsonObject);
        return ajaxResult;
    }

}
