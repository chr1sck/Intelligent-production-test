package com.bsd.say.exception;

/**
 * 业务异常
 */
public class BusinessException extends Exception {

    private String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }
}
