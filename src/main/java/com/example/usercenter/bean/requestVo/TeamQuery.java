package com.example.usercenter.bean.requestVo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.example.usercenter.common.PageRequest;
import lombok.Data;

import java.util.Date;

/**
 * @Author 写你的名字
 * @Date 2023/5/31 22:47
 * @Version 1.0 （版本号）
 */
@Data
public class TeamQuery extends PageRequest {
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
     * 用户id
     */
    private Long userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 搜索关键词（同时对队伍名称和队伍描述查询）
     **/
    private String searchText;
}
