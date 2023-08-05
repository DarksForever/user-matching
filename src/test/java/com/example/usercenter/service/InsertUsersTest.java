package com.example.usercenter.service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.example.usercenter.bean.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @Author 写你的名字
 * @Date 2023/5/26 12:03
 * @Version 1.0 （版本号）
 */
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    @Test
    public void doConcurrencyInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //分10组
        final int BATCH_SIZE=10000;
        int j=0;
        List<CompletableFuture<Void>> futureList=new ArrayList<>();
        for(int i=0;i<10;i++){
            List<User> userList=new ArrayList<>();
            while(true) {
                j++;
                User user = new User();
                user.setUsername("假数据");
                user.setUserAccount("fakeAccount");
                user.setAvatarUrl("https://img1.baidu.com/it/u=1645832847,2375824523&fm=253&fmt=auto&app=138&f=JPEG?w=480&h=480");
                user.setGender(0);
                user.setUserPassword("231231123");
                user.setPhone("12345678912");
                user.setEmail("12345678@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setTags("[]");
                userList.add(user);
                if(j%BATCH_SIZE==0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName:" + Thread.currentThread().getName());
                userService.saveBatch(userList, BATCH_SIZE);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    public void doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=100000;
        List<User> userList=new ArrayList<>();
        for(int i=0;i<INSERT_NUM;i++){
            User user=new User();
            user.setUsername("假数据");
            user.setUserAccount("fakeAccount");
            user.setAvatarUrl("https://img1.baidu.com/it/u=1645832847,2375824523&fm=253&fmt=auto&app=138&f=JPEG?w=480&h=480");
            user.setGender(0);
            user.setUserPassword("231231123");
            user.setPhone("12345678912");
            user.setEmail("12345678@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
