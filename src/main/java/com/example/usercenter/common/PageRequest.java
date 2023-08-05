package com.example.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 写你的名字
 * @Date 2023/5/31 22:50
 * @Version 1.0 （版本号）
 */
@Data
public class PageRequest implements Serializable {
    private static final long serialVersionUID = -1381469223442565631L;
    /**
     * 页面大小
     */
    protected int pageSize;

    /**
     * 当前是第几页
     */
    protected int pageNum;
}
