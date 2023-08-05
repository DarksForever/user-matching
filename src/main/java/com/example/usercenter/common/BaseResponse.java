package com.example.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 *
 * @Author 写你的名字
 * @Date 2023/5/9 22:03
 * @Version 1.0 （版本号）
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = -1871301215562083543L;

    private Integer code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(Integer code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}
