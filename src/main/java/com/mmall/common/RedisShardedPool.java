package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis集群分布式版本.用来管理session
 * ShardedJedis是通过一致性哈希来实现分布式缓存的，通过一定的策略把不同的key分配到不同的redis server上
 */
public class RedisShardedPool {
    private static ShardedJedisPool pool;  //Jedis连接池, static，类初始化前加载
    // 最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    // 最大的空闲状态的Jedis实例的个数
    private static Integer maxIdel =  Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));
    private static Integer minIdel = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    //在borrow一个jedis实例的时候，是否要进行验证操作，如果为true，则得到的jedis实例是可用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    //在归还一个jedis实例的时候，是否要进行验证操作，如果为true，则放回pool的jedis实例是可用的
    private static Boolean testOnRetuen = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdel);
        config.setMinIdle(minIdel);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnRetuen);

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 1000*2);    // 超时时间2秒
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000*2);
        // info1.setPassword();
        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        // 初始化ShardedJedisPool： MURMUR_HASH对应一致性算法
        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH, ShardedJedis.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    public static void returnResource(ShardedJedis jedis) {
        if (jedis != null) {
            // pool.returnResource(jedis); @deprecated
            jedis.close();  // close() 会调用returnBrokenResource或者returnResource
        }
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();
        for (int i = 0; i < 10; i++) {
            jedis.set("key:" + i, "value:"+i);
        }

        returnResource(jedis);

        // pool.destroy(); // 业务不要调
        System.out.println("end");
    }
}
