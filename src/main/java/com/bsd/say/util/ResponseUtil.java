package com.bsd.say.util;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResponseUtil {

//    public static final Logger logger = Logger.getLogger(ResponseUtil.class);

//    private static ObjectMapper objectMapper = JsonUtil.getObjectMapper();

    public static void doResponse(HttpServletResponse response, Object obj) {

//        String jsonStr = null;
        String result = obj.toString();
        try {
            response.getWriter().print(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
