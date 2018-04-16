package com.mmall.util;

import com.mmall.common.RedisPool;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Slf4j
public class RedisPoolUtil {

    public static String set(String key, String value) {
        Jedis jedis = null;
        String result = null;

        jedis = RedisPool.getJedis();
        result = jedis.set(key, value);
        // 2.6.0 捕获异常
        RedisPool.returnResource(jedis);
        return result;
    }

    public static String get(String key) {
        Jedis jedis = RedisPool.getJedis();
        String result = jedis.get(key);
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     *
     * @param expireTime expireTime过期时间，单位秒
     */
    public static String setex(String key, String value, int expireTime) {
        Jedis jedis = RedisPool.getJedis();
        String result = jedis.setex(key,expireTime, value);
        // 2.6.0 捕获异常
        RedisPool.returnResource(jedis);
        return result;
    }

    /**
     * 设置key的有效期
     */
    public static Long expire(String key,int expireTime) {
        Jedis jedis = null;
        Long result = null;

        jedis = RedisPool.getJedis();
        result = jedis.expire(key,expireTime);
        // 2.6.0 捕获异常

        RedisPool.returnResource(jedis);
        return result;
    }

    public static Long del(String key) {
        Jedis jedis = null;
        Long result = null;

        jedis = RedisPool.getJedis();
        result = jedis.del(key);
        RedisPool.returnResource(jedis);
        return result;
    }

    public static void main(String[] args) {
        Jedis jedis = RedisPool.getJedis();
        RedisPoolUtil.set("keyTest", "value");
        String value = RedisPoolUtil.get("keyTest");
        RedisPoolUtil.setex("keyex", "valueex", 1000);
        RedisPoolUtil.expire("keyTest", 60*10);
        RedisPoolUtil.del("keyTest");
        System.out.println("end");
    }
}
