package com.example.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author 写你的名字
 * @Date 2023/5/30 22:31
 * @Version 1.0 （版本号）
 */
@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test() {
        RList<String> testList = redissonClient.getList("testList");
        //testList.add("darkness");
        System.out.println(testList.get(0));
        testList.remove(0);
    }

    @Test
    public void preCacheTest(){
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try{
            //只有一个线程能获取到锁
            if (lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(30000);
                System.out.println("getLock: "+Thread.currentThread().getId());
            }
        }catch(InterruptedException e){
            System.out.println(e.getMessage());
        }finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()){
                System.out.println("unlock: "+Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
