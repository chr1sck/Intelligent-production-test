package com.bsd.say.exception;

// 为了简短，省略了部分代码
public class ParamsException extends RuntimeException {

    private String code;


    public ParamsException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ParamsException(String message) {
        super(message);
    }
}