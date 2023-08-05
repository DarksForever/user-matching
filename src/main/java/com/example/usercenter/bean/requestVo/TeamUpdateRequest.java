package com.example.usercenter.bean.requestVo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @BelongsProject: user-center
 * @BelongsPackage: com.example.usercenter.bean.requestVo
 * @Author: liaocy
 * @CreateTime: 2023-06-04  21:02
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = -2083704474102076398L;
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
