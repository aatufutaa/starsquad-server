package git.aatufutaa.lobby.mongo;

import git.aatufutaa.lobby.util.Hashids;

public class PlayerId {

    private static final Hashids PLAYER_ID_HASH = new Hashids("TmEmw5hPgsJF5zzSVu7bafjFAqr1BjCm", 8);

    public static int parsePlayerId(String hash) {
        if (hash.startsWith("#")) hash = hash.substring(1);
        return (int) PLAYER_ID_HASH.decode(hash.toUpperCase())[0];
    }

    public static String convertIdToHash(int playerId) {
        return PLAYER_ID_HASH.encode(playerId);
    }
}
