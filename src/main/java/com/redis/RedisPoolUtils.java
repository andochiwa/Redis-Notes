package com.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author HAN
 * @version 1.0
 * @create 03-13-0:05
 */
public class RedisPoolUtils {

    private static JedisPool jedisPool;

    public static JedisPool getJedisPoolInstance() {
        if (jedisPool == null) {
            synchronized (RedisPoolUtils.class) {
                if (jedisPool == null) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxIdle(10);
                    jedisPool = new JedisPool(config, "localhost", 6379);
                }
            }
        }
        return jedisPool;
    }

    public static void release(Jedis jedis) {
        if (jedis != null)
            jedis.close();
    }

}
