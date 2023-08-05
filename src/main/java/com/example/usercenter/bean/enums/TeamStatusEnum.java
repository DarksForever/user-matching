package com.example.usercenter.bean.enums;

/**
 * 队伍状态枚举
 * @Author 写你的名字
 * @Date 2023/6/1 14:58
 * @Version 1.0 （版本号）
 */
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    private int value;
    private String text;

    TeamStatusEnum(int value,String text){
        this.value=value;
        this.text=text;
    }

    public static TeamStatusEnum getEnumByValue(Integer value){
        if(value==null){
            return null;
        }
        TeamStatusEnum[] enums = TeamStatusEnum.values();
        for(TeamStatusEnum cur:enums){
            if(cur.getValue()==value){
                return cur;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
