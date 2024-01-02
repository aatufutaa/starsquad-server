package git.aatufutaa.login.redis;

import lombok.Getter;
import redis.clients.jedis.JedisPooled;

public class RedisManager {

    @Getter
    private JedisPooled jedisPool;

    public RedisManager() {
    }

    public void start() {
        this.jedisPool = new JedisPooled("localhost", 6379);
    }

    public void close() {
        this.jedisPool.close();
    }
}
