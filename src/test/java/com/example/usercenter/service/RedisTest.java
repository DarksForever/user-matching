package com.example.usercenter.service;

import com.example.usercenter.bean.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @Author 写你的名字
 * @Date 2023/5/30 10:30
 * @Version 1.0 （版本号）
 */
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
//        valueOperations.set("yupiString","dog");
//        valueOperations.set("yupiInt",1);
//        valueOperations.set("yupiDouble",2.0);
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("yupi");
//        valueOperations.set("yupiUser",user);
        //查
        Object yupiString = valueOperations.get("yupiString");
        System.out.println(yupiString);
        Object yupiInt = valueOperations.get("yupiInt");
        System.out.println(yupiInt);
        Object yupiDouble = valueOperations.get("yupiDouble");
        System.out.println(yupiDouble);
        Object yupiUser = valueOperations.get("yupiUser");
        System.out.println(yupiUser);
        //删
        redisTemplate.delete("yupiUser");
    }
}
