package com.bsd.say.controller;

import com.bsd.say.util.ResponseUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;

@RestController
@RequestMapping("wechat")
public class WechatController {
    /**
     * 接收component_verify_ticket 或 authorized事件
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "getComponentVerifyTicket")
    public void getComponentVerifyTicket(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        logger.info("接收component_verify_ticket 或 authorized事件");
        String nonce = request.getParameter("nonce");
        String timestamp = request.getParameter("timestamp");
        String msgSignature = request.getParameter("msg_signature");

        StringBuilder sb = new StringBuilder();
        BufferedReader in = request.getReader();
        String line;
        while((line = in.readLine()) != null) {
            sb.append(line);
        }
        String postData = sb.toString();
//        logger.info("nonce: " + nonce);
//        logger.info("timestamp: " + timestamp);
//        logger.info("msgSignature: " + msgSignature);
//        logger.info("postData: " + postData);
//        thirdPartyService.getComponentVerifyTicket(timestamp, nonce, msgSignature, postData);
        ResponseUtil.doResponse(response, "success");
//    return "success";
    }

}
