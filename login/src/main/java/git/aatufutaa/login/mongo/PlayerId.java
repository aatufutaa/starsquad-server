package git.aatufutaa.login.mongo;

public class PlayerId {

    private static final Hashids PLAYER_ID_HASH = new Hashids("TmEmw5hPgsJF5zzSVu7bafjFAqr1BjCm", 8);

    public static int parsePlayerId(String hash) {
        return (int) PLAYER_ID_HASH.decode(hash)[0];
    }

    public static String convertIdToHash(int playerId) {
        return PLAYER_ID_HASH.encode(playerId);
    }
}
