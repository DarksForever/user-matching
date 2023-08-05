package com.example.usercenter.bean.requestVo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册实体
 * @Author 写你的名字
 * @Date 2023/5/7 16:56
 * @Version 1.0 （版本号）
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 8577483620725036578L;

    private String userAccount;
    private String userPassword;
}
