package com.mmall.util;

import com.mmall.common.RedisShardedPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ShardedJedis;

@Slf4j
public class RedisShardedPoolUtil {
    public static String set(String key, String value) {
        ShardedJedis jedis = null;
        String result = null;

        jedis = RedisShardedPool.getJedis();
        result = jedis.set(key, value);
        // 2.6.0 捕获异常
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static String get(String key) {
        ShardedJedis jedis = RedisShardedPool.getJedis();
        String result = jedis.get(key);
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     *
     * @param expireTime expireTime过期时间，单位秒
     */
    public static String setex(String key, String value, int expireTime) {
        ShardedJedis jedis = RedisShardedPool.getJedis();
        String result = jedis.setex(key,expireTime, value);
        // 2.6.0 捕获异常
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置key的有效期
     */
    public static Long expire(String key,int expireTime) {
        ShardedJedis jedis = null;
        Long result = null;

        jedis = RedisShardedPool.getJedis();
        result = jedis.expire(key,expireTime);
        // 2.6.0 捕获异常

        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static Long del(String key) {
        ShardedJedis jedis = null;
        Long result = null;

        jedis = RedisShardedPool.getJedis();
        result = jedis.del(key);
        RedisShardedPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        ShardedJedis jedis = RedisShardedPool.getJedis();
        RedisPoolUtil.set("keyTest", "value");
        String value = RedisPoolUtil.get("keyTest");
        RedisPoolUtil.setex("keyex", "valueex", 1000);
        RedisPoolUtil.expire("keyTest", 60*10);
        RedisPoolUtil.del("keyTest");
        System.out.println("end");
    }

    /**
     * 获取分布式锁的方法
     */
    public static Long setnx(String key, String value) {
        ShardedJedis jedis = RedisShardedPool.getJedis();
        Long result = jedis.setnx(key, value);

        RedisShardedPool.returnResource(jedis);
        return result;
    }

    /**
     * 具有原子性
     */
    public static String getSet(String key, String value) {
        ShardedJedis jedis = RedisShardedPool.getJedis();
        String result = jedis.getSet(key, value);

        RedisShardedPool.returnResource(jedis);
        return result;
    }

}
