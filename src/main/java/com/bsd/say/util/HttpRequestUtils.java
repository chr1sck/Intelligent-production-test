package com.bsd.say.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class HttpRequestUtils {

    private static CloseableHttpClient httpClient = HttpClients.createDefault();
    private static HttpClientContext context = new HttpClientContext();


    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url   发送请求的URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {

        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            result = sendGet(urlNameString);
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String sendGet(String url) {

        CloseableHttpResponse response = null;
        String content = null;
        try {
            HttpGet get = new HttpGet(url);
            response = httpClient.execute(get, context);
            HttpEntity entity = response.getEntity();
            content = EntityUtils.toString(entity);
            EntityUtils.consume(entity);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return content;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url   发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param, String token) {

        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


    public static String sendPost(String url, JSONObject jsonObject) throws IOException {


        CloseableHttpClient httpClient = HttpClients.createDefault();
        StringEntity entity;
        //url的get请求
        HttpPost post = new HttpPost(url);
        //设置长连接
        post.setHeader("Connection", "keep-alive");
        //模拟游览器,游览器中输入about://version查看代理项,模拟自己的游览器
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");

        entity = new StringEntity(jsonObject.toJSONString(), "utf-8");        // 解决中文乱码问题
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        post.setEntity(entity);

        RequestConfig defaultConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        post.setConfig(defaultConfig);
        //执行请求,并获取回应
        CloseableHttpResponse httpResponse = httpClient.execute(post);
        //成功获取json数据
        String result = EntityUtils.toString(httpResponse.getEntity());

        return result;
    }


    public static void main(String[] args) throws IOException {

        String url = "http://10.10.2.184:1292/token";
        String appId = "8ec5fe4f-1213-4a2f-80e4-fc8887e71122";
        String secert = "123456";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(url).append("?appId=").append(appId).append("&secret=").append(secert);
        url = stringBuilder.toString();

        String result = HttpRequestUtils.sendGet(url);
        String token = JSONObject.parseObject(result).getString("data");

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpResponse httpResponse = null;

        try {
            String psncode = "160647";
            String str = sendPost("http://10.10.2.184:1292/hr/QueryAnnual?psncode=160647", "", token);

            System.out.println(str);
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

            httpclient.close();
        }


    }

}