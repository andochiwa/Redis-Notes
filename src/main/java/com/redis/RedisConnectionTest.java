package com.redis;

import redis.clients.jedis.Jedis;

/**
 * @author HAN
 * @version 1.0
 * @create 03-12-23:20
 */
public class RedisConnectionTest {

    public static void main(String[] args){
        Jedis jedis = new Jedis("localhost", 6379);
        System.out.println(jedis.ping());

        jedis.set("k1", "v1");
        jedis.set("k2", "v2");
        jedis.set("k3", "v3");
    }

}
