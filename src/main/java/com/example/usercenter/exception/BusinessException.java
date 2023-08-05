package com.example.usercenter.exception;

import com.example.usercenter.common.ErrorCode;

/**
 * @Author 写你的名字
 * @Date 2023/5/9 22:26
 * @Version 1.0 （版本号）
 */
public class BusinessException extends RuntimeException{
    private final int code;
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code= errorCode.getCode();
        this.description=errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode,String description){
        super(errorCode.getMessage());
        this.code= errorCode.getCode();
        this.description=description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
