package com.example.usercenter.bean.requestVo;

import lombok.Data;

import java.io.Serializable;

/**
 * @BelongsProject: user-center
 * @BelongsPackage: com.example.usercenter.bean.requestVo
 * @Author: liaocy
 * @CreateTime: 2023-06-04  22:34
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class JoinTeamRequest implements Serializable {
    private static final long serialVersionUID = -4917922826710878349L;
    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}
