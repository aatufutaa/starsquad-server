package git.aatufutaa.game.mongo;

import git.aatufutaa.game.GameServer;
import org.bson.Document;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPooled;

public class PlayerDataCache {

    public enum CacheError {
        OK,
        PLAY_NOT_FOUND,
        FAILED_TO_SAVE_CACHE
    }

    public record CacheResult(Document document, CacheError error) {
    }

    private static String getJedisKey(int playerId) {
        return "cached_" + GameServer.MASTER_VERSION + "_player_data_" + playerId;
    }

    public static void clearCache(Jedis jedis, int playerId) {
        String jedisKey = getJedisKey(playerId);
        jedis.del(jedisKey);
    }

    public static CacheResult fetchData(JedisPooled jedis, int playerId, boolean checkForErrors) {
        String jedisKey = getJedisKey(playerId);
        String cachedResult = jedis.get(jedisKey);

        if (cachedResult != null) {
            // cache hit
            if (cachedResult.equals("no_data"))
                return new CacheResult(null, CacheError.PLAY_NOT_FOUND); // makes sure looking up players who dont exist also gets cached

            Document document = Document.parse(cachedResult);
            return new CacheResult(document, CacheError.OK);
        }

        // cache miss
        Document document = GameServer.getInstance().getGameMongoManager().getPlayers().find(new Document("_id", playerId)).first();

        if (document != null) {
            if (jedis.setnx(jedisKey, document.toJson()) != 1) { // if data got updated during lookup fail
                if (checkForErrors)
                    return new CacheResult(null, CacheError.FAILED_TO_SAVE_CACHE);
            } else {
                // what if crash happens here -> key would be stored forever
                jedis.expire(jedisKey, 60 * 30); // store for 30min (may need to change later)
            }
        } else {
            if (jedis.setnx(jedisKey, "no_data") != 1) {  // if data got updated during lookup
                if (checkForErrors)
                    return new CacheResult(null, CacheError.FAILED_TO_SAVE_CACHE);
            } else {
                // what if crash happens here -> key would be stored forever
                jedis.expire(jedisKey, 60 * 30); // store for 30min (may need to change later)
            }
        }

        if (document == null) {
            return new CacheResult(null, CacheError.PLAY_NOT_FOUND);
        }

        return new CacheResult(document, CacheError.OK);
    }
}
