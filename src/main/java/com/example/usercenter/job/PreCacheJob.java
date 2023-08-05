package com.example.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.usercenter.bean.User;
import com.example.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author 写你的名字
 * @Date 2023/5/30 11:11
 * @Version 1.0 （版本号）
 */
@Slf4j
@Component
public class PreCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList= Arrays.asList(1654485476406587394L);

    //每天执行，预热用户
    @Scheduled(cron = "0 50 12 * * *")
    public void doCacheRecommendUser(){
        String REDIS_LOCK_KEY="yupao:preCacheJob:doCache:lock";
        RLock rLock = redissonClient.getLock(REDIS_LOCK_KEY);
        try {
            //waitTime=0,表示只有一个线程能拿到锁（其他线程等待0s就不再执行）
            if(rLock.tryLock(0L,-1,TimeUnit.MILLISECONDS)) {
                System.out.println("getlock:"+Thread.currentThread().getName());
                for (Long userId : mainUserList) {
                    QueryWrapper<User> wrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), wrapper);
                    String redisKey = String.format("yupao:user:recommend:%s", userId);
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    //写缓存,即使缓存有异常，也不应该影响到业务的正常执行
                    try {
                        //所有写入Redis中的数据一定要设置过期时间
                        valueOperations.set(redisKey, userPage, 1000000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        //打印日志告知缓存异常即可，不需要抛出异常
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error"+e);
        } finally {
            //一定要释放锁，注意只能释放自己的锁
            if(rLock.isHeldByCurrentThread()){
                System.out.println("unlock:"+Thread.currentThread().getName());
                rLock.unlock();
            }
        }
    }
}
