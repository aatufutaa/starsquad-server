package git.aatufutaa.login.mongo;

import git.aatufutaa.login.LoginServer;
import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.InsertOneResult;
import lombok.AllArgsConstructor;
import org.bson.Document;
import redis.clients.jedis.Jedis;

public class LoginManager {

    private static final int PLAYER_DATA_VERSION = 1;

    public enum AuthCode {
        CREATED,
        EXIST,
        DATA_BASE_ERROR,
        BROKEN_LINK,
        RATE_LIMIT
    }

    public static class AuthenticationResponse {

        public AuthCode code;
        public Document document;

        public AuthenticationResponse(AuthCode code) {
            this.code = code;
        }

        public AuthenticationResponse(AuthCode code, Document document) {
            this.code = code;
            this.document = document;
        }
    }

    @AllArgsConstructor
    public static class LoginInfo {
        private String ip;

        private boolean android;
        private String gameCenter;
        private String token;
    }

    private static boolean rateLimit(String key, int time, int max) {
        try (Jedis jedis = LoginServer.getInstance().getRedisManager().getResource()) {

            String rateLimitRaw = jedis.get(key);

            if (rateLimitRaw != null) {
                int num = Integer.parseInt(rateLimitRaw);
                if (num > max) {
                    return true;
                }
            }

            jedis.incrBy(key, 1);
            jedis.expire(key, time);

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static AuthenticationResponse createAccount(LoginInfo loginInfo) {
        System.out.println("creating a new account");

        // rate limit account creation to 10 per 10min hour (in case someone makes accounts in public)
        if (rateLimit("accountCreation:" + loginInfo.ip, 600, 10)) {
            return new AuthenticationResponse(AuthCode.RATE_LIMIT);
        }

        int id = LoginServer.getInstance().getLoginMongoManager().generatePlayerId();
        String token = TokenGenerator.generateToken(id);
        System.out.println("Generating new account id " + id + " token " + token);

        Document document = new Document("_id", id);
        document.put("token", token);

        //document.put("state", "tutorial");

        document.put("create_ip", loginInfo.ip);

        // TODO: save device information

        //document.put("selected_hero", Hero.TEST_123123.getId());

        //document.put("level", 0);
        //document.put("coins", 500);
        //document.put("gems", 30);

        document.put("location", 0); // TODO: find

        // just so server knows what version playerdata was created on in case of updates
        document.put("version", PLAYER_DATA_VERSION);

        // auto link game center account
        if (loginInfo.gameCenter != null) {
            document.put(loginInfo.android ? "google_play" : "game_center", loginInfo.gameCenter);
        }

        // if somehow game center got linked during creation process
        try {
            InsertOneResult result = LoginServer.getInstance().getLoginMongoManager().getPlayers().insertOne(document);
        } catch (MongoWriteException e) {
            System.out.println("CANT insert " + document);
            e.printStackTrace();
            return new AuthenticationResponse(AuthCode.DATA_BASE_ERROR);
        }

        return new AuthenticationResponse(AuthCode.CREATED, document);
    }

    public static AuthenticationResponse login(LoginInfo loginInfo) {
        System.out.println("authenticating");
        System.out.println("android " + loginInfo.android);
        System.out.println("token " + loginInfo.token);
        System.out.println("game_center " + loginInfo.gameCenter);

        // rate limiting IP to 14 logins per 1 min
        if (rateLimit("login:" + loginInfo.ip, 60, 14)) {
            return new AuthenticationResponse(AuthCode.RATE_LIMIT);
        }

        if (loginInfo.gameCenter != null) {

            String gameCenterKey = loginInfo.android ? "google_play" : "game_center";

            Document document = LoginServer.getInstance().getLoginMongoManager().getPlayers().find(new Document(gameCenterKey, loginInfo.gameCenter)).first();

            // account found by game center
            if (document != null) {
                return new AuthenticationResponse(AuthCode.EXIST, document);
            }

            // the only case this should happen
            // player has been playing without game center, then logs in and gets auto linked
            if (loginInfo.token != null) {
                document = LoginServer.getInstance().getLoginMongoManager().getPlayers().find(new Document("token", loginInfo.token)).first();

                // account found by id
                if (document != null) {

                    // account not linked, auto link
                    boolean hasLinkedAccount = document.getString("game_center") != null || document.getString("google_play") != null;
                    if (!hasLinkedAccount) {
                        try {
                            LoginServer.getInstance().getLoginMongoManager().getPlayers().updateOne(new Document("_id", document.getInteger("_id")),
                                    Updates.set(gameCenterKey, loginInfo.gameCenter));
                        } catch (MongoWriteException e) {
                            // this is for possible duplicate issues
                            System.out.println("cant update game center for " + document.getInteger("_id"));
                            e.printStackTrace();
                            return new AuthenticationResponse(AuthCode.DATA_BASE_ERROR);
                        }
                    } else {
                        // this should not happend
                        // if the account was already linked, login should happen using game center
                        // if the account use different game center, this still shouldnt happen
                        return new AuthenticationResponse(AuthCode.BROKEN_LINK);
                    }

                    return new AuthenticationResponse(AuthCode.EXIST, document);
                }
            }

            // account not found
            // create new
            return createAccount(loginInfo);
        }

        // below code only happen when player is not logged in game center
        if (loginInfo.token != null) {
            Document document = LoginServer.getInstance().getLoginMongoManager().getPlayers().find(new Document("token", loginInfo.token)).first();

            // account found by id
            if (document != null) {

                // if account is linked, login is required
                boolean hasLinkedAccount = document.getString("game_center") != null || document.getString("google_play") != null;
                if (hasLinkedAccount) {
                    // create new account
                    return createAccount(loginInfo);
                }

                return new AuthenticationResponse(AuthCode.EXIST, document);
            }

            // account not found // deleted?
            // create new account
            return createAccount(loginInfo);
        }

        // player starts the app first time without game center
        // create new account
        return createAccount(loginInfo);
    }
}
