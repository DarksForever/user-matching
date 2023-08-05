package com.example.usercenter.bean.Vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @BelongsProject: user-center
 * @BelongsPackage: com.example.usercenter.bean.Vo
 * @Author: liaocy
 * @CreateTime: 2023-06-06  09:57
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class TeamVO implements Serializable {
    private static final long serialVersionUID = -1935158459020082650L;
    /**
     * id
     */
    private Long id;

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
