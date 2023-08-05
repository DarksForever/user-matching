package com.example.usercenter.bean.requestVo;

import lombok.Data;

import java.io.Serializable;

/**
 * @BelongsProject: user-center
 * @BelongsPackage: com.example.usercenter.bean.requestVo
 * @Author: liaocy
 * @CreateTime: 2023-06-05  21:41
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class QuitTeamRequest implements Serializable {
    private static final long serialVersionUID = 1444463786991779933L;
    /**
     * 队伍id
     **/
    private Long teamId;
}
