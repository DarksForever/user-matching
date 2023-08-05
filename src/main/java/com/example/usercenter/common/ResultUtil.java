package com.example.usercenter.common;

/**
 * @Author 写你的名字
 * @Date 2023/5/9 22:05
 * @Version 1.0 （版本号）
 */
public class ResultUtil {
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }

    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code,null,message,description);
    }

    public static BaseResponse error(ErrorCode systemError, String message, String description) {
        return new BaseResponse<>(systemError.getCode(),null,message,description);
    }
}
