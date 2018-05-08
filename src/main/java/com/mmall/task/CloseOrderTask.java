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

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedissonManager redissonManager;

    // @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟(整数倍)执行一次
    public void closeOrderTaskV1() {
        log.info("关闭订单，定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        orderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }


    /**
     * task v2存在的问题:如果tomcat被关闭（如kill方式), 如果此时setnx成功，则关闭后，如果锁来不及删除，造成死锁
     * 可以使用tomcat的shutdown命令来关闭: 通过@PreDestroy注解，来在关闭时释放锁。缺点: 如果锁数量太多，则耗时会长，并且对kill没用
     */
    // @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟(整数倍)执行一次
    public void closeOrderTaskV2() {
        log.info("关闭订单定时任务启动");

        Long timeout = Long.valueOf(PropertiesUtil.getProperty("lock.time", "5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + timeout));  // value 尚未被使用
        if (setnxResult != null && setnxResult == 1) {
            // 成功获取锁
            log.info("成功获取分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, timeout.intValue());
        }
        else {
            log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }

        log.info("关闭订单定时任务结束");
    }

    /**
     * v2的优化
     */
    @PreDestroy
    public void delLock() {
        RedisShardedPoolUtil.del(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
    }



    /**
     * 原生实现分布式锁
     *  利用锁的值: 时间戳+超时过期时间
     */
    @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟(整数倍)执行一次
    public void closeOrderTaskV3() {
        log.info("关闭订单定时任务启动");

        String lockName = Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK;
        Long timeout = Long.valueOf(PropertiesUtil.getProperty("lock.time", "5000"));
        Long setnxResult = RedisShardedPoolUtil.setnx(lockName,
                String.valueOf(System.currentTimeMillis() + timeout));  // value 尚未被使用
        if (setnxResult != null && setnxResult == 1) {
            // 成功获取锁
            log.info("成功获取分布式锁: {}", lockName);
            closeOrder(lockName, timeout.intValue());
        }
        else {
            // 未获取到锁，判断是否可以重置并获取到锁，即锁已超时但未被删除
            // log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            String lockValue = RedisShardedPoolUtil.get(lockName);
            if (lockValue != null && System.currentTimeMillis() > Long.parseLong(lockValue)) {
                // 此时锁已失效， 即使锁是没有设置expire的
                // 重置锁的时间，设置新值(时间戳+timeout)，返回最新的旧值
                String getSetResult = RedisShardedPoolUtil.getSet(lockName, String.valueOf(System.currentTimeMillis() + timeout));
                // 用旧值判断是否可以获取锁:
                // 1. 当key没有旧值，即key此时已经不存在 -> 返回nil -> 可以获取锁
                // 2. 锁的value没有被其他进程set, 重置成功，此时有权力获取锁
                if (getSetResult == null || (getSetResult != null && StringUtils.equals(getSetResult, lockValue))) {
                    // 真正获取了锁
                    closeOrder(lockName, timeout.intValue());
                }
                else {
                    log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }
            else {
                // 锁尚未失效, 无法获取
                log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }
        }
        log.info("关闭订单定时任务结束");
    }

    /**
     * v2和v3通用的关闭订单方法
     */
    private void closeOrder(String lockName, int timeout) {
        RedisShardedPoolUtil.expire(lockName, timeout); // 设置锁过期时间
        log.info("获取{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread());
        // 关闭超过hour小时未支付的订单
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        orderService.closeOrder(hour);
        RedisShardedPoolUtil.del(lockName); // 释放锁
        log.info("释放{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread());
        log.info("====================================");
    }

    //@Scheduled(cron = "0 */1 * * * ?")
    public void closeOrderTaskV4() {
        // 分布式可重入锁
        RLock lock = redissonManager.getRedisson().getLock(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK); // 锁的名字
        boolean getLock = false;
        try {
            if (getLock = lock.tryLock(0, 50, TimeUnit.SECONDS)) {  // timewait设置为0, 避免同时获取锁
                log.info("Redisson获取分布式锁{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread());

                int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
                // orderService.closeOrder(hour);
            }
            else {
                log.info("Redisson没有获取分布式锁{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread());
            }
        } catch (InterruptedException e) {
            log.error("Redisson获取分布式锁异常", e);
        } finally {
            if (!getLock) return;
            lock.unlock();
            log.info("Redisson释放分布式锁{}, ThreadName:{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK, Thread.currentThread());

        }
    }

}
