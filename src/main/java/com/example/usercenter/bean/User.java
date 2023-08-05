package com.example.usercenter.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户id
     */
    private Long id;

    /**
     * 昵称
     */
    private String username;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态（0-正常）
     */
    private Integer userStatus;

    /**
     * 用户角色(0-普通用户 1-管理员)
     */
    private Integer userRole;

    /*
     *  用户标签
     */
    private String tags;

    /**
     * 数据插入时间
     */
    private Date createTime;

    /**
     * 数据更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer isdeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}