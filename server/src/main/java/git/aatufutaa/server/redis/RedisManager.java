package git.aatufutaa.server.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisManager {

    private JedisPool jedisPool;

    private JedisPoolConfig buildPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    public RedisManager() {
    }

    public void start() {
        this.jedisPool = new JedisPool(this.buildPoolConfig(), "localhost", 6379);
    }

    public void close() {
        this.jedisPool.close();
    }

    public Jedis getResource() {
        return this.jedisPool.getResource();
    }
}
