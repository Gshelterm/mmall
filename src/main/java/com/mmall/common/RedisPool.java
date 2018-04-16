package com.mmall.common;


import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool jedisPool;  //Jedis连接池, static，类初始化前加载
    // 最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total", "20"));
    // 最大的空闲状态的Jedis实例的个数
    private static Integer maxIdel =  Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle", "10"));
    private static Integer minIdel = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle", "2"));
    //在borrow一个jedis实例的时候，是否要进行验证操作，如果为true，则得到的jedis实例是可用的
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow", "true"));
    //在归还一个jedis实例的时候，是否要进行验证操作，如果为true，则放回pool的jedis实例是可用的
    private static Boolean testOnRetuen = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return", "true"));

    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdel);
        config.setMinIdle(minIdel);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnRetuen);

        // 默认为true config.setBlockWhenExhausted(true); 连接耗尽时是否阻塞,true阻塞直到超时,false抛出异常
        jedisPool = new JedisPool(config, redisIp, redisPort, 1000*2);
    }

    static {
        initPool();
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void returnResource(Jedis jedis) {
        if (jedis != null) {
            // jedisPool.returnResource(jedis); @deprecated
            jedis.close();  // close() 会调用returnBrokenResource或者returnResource
        }
    }

    public static void main(String[] args) {
        Jedis jedis = jedisPool.getResource();
        jedis.set("mykey", "myvalue");
        returnResource(jedis);

        jedisPool.destroy(); // 业务不要调
        System.out.println("end");
    }

}
