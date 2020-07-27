package com.bsd.say.beans;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yaoyao.zhu
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONType
public class AjaxRequest {

    private String requestId;
    private String method;
    private String caller;
    private JSONObject data = new JSONObject();
    private String userKey;
    private String paramSign;
    private String callTime = String.valueOf(System.currentTimeMillis());

}
