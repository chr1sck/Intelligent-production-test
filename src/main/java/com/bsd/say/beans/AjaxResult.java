package com.bsd.say.beans;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装返回数据
 * @author yaoyao.zhu
 * @date
 */
@Data

@NoArgsConstructor
@JSONType
public class AjaxResult {

    private int retcode = 1;
    public static final int SUCCESS = 1;
    public static final int FAILED = 0;
    private String retmsg = "操作成功";
    private Object data;

}
