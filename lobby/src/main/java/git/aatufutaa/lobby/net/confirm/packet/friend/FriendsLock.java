package git.aatufutaa.lobby.net.confirm.packet.friend;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;

public class FriendsLock {

    public static boolean lock(Jedis jedis, int playerId, int friendId) {
        String lock1 = "friend_lock_" + playerId;
        String lock2 = "friend_lock_" + friendId;

        if (jedis.setnx(lock1, "") != 1) {
            return false;
        }
        if (jedis.setnx(lock2, "") != 1) {
            jedis.del(lock1);
            return false;
        }

        jedis.expire(lock1, 60);
        jedis.expire(lock2, 60);

        return true;
    }

    public static void unlock(Jedis jedis, int playerId, int friendId) {
        String lock1 = "friend_lock_" + playerId;
        String lock2 = "friend_lock_" + friendId;

        jedis.del(lock1, lock2);
    }
}
