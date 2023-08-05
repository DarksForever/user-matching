package com.example.usercenter.bean.requestVo;

import lombok.Data;

import java.util.Date;

/**
 * @Author 写你的名字
 * @Date 2023/6/1 15:41
 * @Version 1.0 （版本号）
 */
@Data
public class TeamAddRequest {
    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;
}
