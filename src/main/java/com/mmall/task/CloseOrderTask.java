package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.common.RedisShardedPool;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CloseOrderTask {

    @Autowired
    private IOrderService orderService;

    // @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟(整数倍)执行一次
    public void closeOrderTaskV1() {
        log.info("关闭订单，定时任务启动");
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour", "2"));
        orderService.closeOrder(hour);
        log.info("关闭订单定时任务结束");
    }


    @Scheduled(cron = "0 */1 * * * ?")  // 每一分钟(整数倍)执行一次
    public void closeOrderTaskV2() {
        log.info("关闭订单，定时任务启动");

        long timeout = Long.parseLong(PropertiesUtil.getProperty("lock.time", "5000"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,
                String.valueOf(System.currentTimeMillis() + timeout));  // value 尚未被使用
        if (setnxResult != null && setnxResult == 1) {
            // 成功获取锁
            log.info("成功获取分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);

        }
        else {
            log.info("没有获得分布式锁: {}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }

        log.info("关闭订单定时任务结束");
    }

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

}
