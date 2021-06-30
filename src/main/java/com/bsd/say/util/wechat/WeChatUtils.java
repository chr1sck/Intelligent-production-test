package com.bsd.say.util.wechat;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class WeChatUtils {


    /**
     * 统一回复微信服务器
     * @param response
     * @param content
     * @throws IOException
     */
    public static void responseReplyMessage(HttpServletResponse response, String content) throws IOException {
        PrintWriter pw = response.getWriter();
        pw.write(content);
        pw.flush();
        pw.close();
    }
}
