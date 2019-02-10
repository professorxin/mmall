package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedissonManager;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService iOrderService;

    @Autowired
    private RedissonManager redissonManager;

    //    @Scheduled(cron = "0 */1 * * * ?")//每1分钟（每个1分钟的整数倍）
    public void closeOrderTaskV1() {
        log.info("关闭订单定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//        iOrderService.closeOrder(hour);
        log.info("关闭订单定时任务关闭");
    }

    //    @Scheduled(cron = "0 */1 * * * ?")//每1分钟（每个1分钟的整数倍）
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");
        Long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "50000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            log.info("没有获取分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }
        log.info("关闭订单定时任务关闭");
    }

    //    @Scheduled(cron = "0 */1 * * * ?")//每1分钟（每个1分钟的整数倍）
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");
        Long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.timeout", "50000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
        if (setnxResult != null && setnxResult.intValue() == 1) {
            //如果返回值是1，代表设置成功，获取锁
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        } else {
            //未获取到锁，继续判断，判断时间戳，看是否可以重置并获取到锁
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if (lockValueStr != null && System.currentTimeMillis() > Long.parseLong(lockValueStr)) {
                //原子操作，设置新的值并且返回之前旧的值
                String getSetResult = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, String.valueOf(System.currentTimeMillis() + lockTimeout));
                //返回旧的值，当为空时，证明其他进程已经删除该键值对，可以进行业务操作。
                //返回旧的值和重新设置新的值时的旧的值相同，证明没有其他进程打扰，可以进行业务操作。
                if (getSetResult == null || StringUtils.equals(lockValueStr, getSetResult)) {
                    //真正获取到锁
                    closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                } else {
                    log.info("没有获取分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            } else {
                log.info("没有获取分布式锁：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务关闭");
    }

    @Scheduled(cron = "0 */1 * * * ?")//每1分钟（每个1分钟的整数倍）
    public void closeOrderTaskV4() {
        log.info("关闭订单定时任务启动");
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        boolean getLock = false;
        try {
            if (getLock = lock.tryLock(0, 50, TimeUnit.SECONDS)) {
                log.info("Redisson获取到分布式锁：{}，ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//                iOrderService.closeOrder(hour);
            } else {
                log.info("Redisson没有获取到分布式锁：{}，ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
            }
        } catch (InterruptedException e) {
            log.error("Redisson获取分布式锁异常", e);
        } finally {
            if (!getLock) {
                return;
            }
            lock.unlock();
            log.info("Redisson释放分布式锁");
        }
        log.info("关闭订单定时任务关闭");
    }

    private void closeOrder(String lockName) {
        RedisShardedPoolUtil.expire(lockName, 50);
        log.info("获取：{}，ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
//        iOrderService.closeOrder(hour);
        RedisShardedPoolUtil.del(lockName);
        log.info("释放：{}，ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread().getName());
        log.info("========================");
    }

}
