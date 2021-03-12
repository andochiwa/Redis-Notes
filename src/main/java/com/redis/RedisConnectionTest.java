package com.redis;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Set;

/**
 * @author HAN
 * @version 1.0
 * @create 03-12-23:20
 */
public class RedisConnectionTest {

    Jedis jedis = new Jedis("localhost", 6379);

    @Test
    void testConnection(){
        System.out.println(jedis.ping());

        jedis.set("k1", "v1");
        jedis.set("k2", "v2");
        jedis.set("k3", "v3");
        Set<String> keys = jedis.keys("*");
        keys.forEach(System.out::println);
    }

    @Test
    void testTx() {
        Transaction transaction = jedis.multi();

        transaction.set("k4", "v4");
        transaction.set("k5", "v5");
        transaction.set("k6", "v6");

        transaction.exec();
    }

    @Test
    void testPool() {
        Jedis jedis2 = RedisPoolUtils.getJedisPoolInstance().getResource();
        System.out.println(jedis2);
    }

}
