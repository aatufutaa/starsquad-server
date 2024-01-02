package git.aatufutaa.login.net.packet;

import git.aatufutaa.login.LoginServer;
import git.aatufutaa.login.master.LoginMasterIncomingPacket;
import git.aatufutaa.login.master.LoginMasterOutgoingPacket;
import git.aatufutaa.login.mongo.PlayerId;
import git.aatufutaa.login.mongo.TokenGenerator;
import git.aatufutaa.login.net.client.LoginClient;
import git.aatufutaa.login.net.client.ProtocolState;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.MasterConnection;
import git.aatufutaa.server.communication.packet.packets.WaitForResponseIncomingPacket;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import com.mongodb.MongoWriteException;
import com.mongodb.client.result.InsertOneResult;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;

public class LoginIncomingPacket implements IncomingPacket {

    private static final String RATE_LIMIT_TEXT = "You are currently being rate limited. Try again later.";
    private static final String SERVER_ERROR = "Server error. Try again later.";

    private String token; // token
    // TODO: other useful information ? model, name, os

    @Override
    public void read(ByteBuf buf) throws Exception {
        if (buf.readBoolean()) {
            this.token = ByteBufUtil.readString(buf, 64);
        }
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        LoginClient loginClient = (LoginClient) client;

        loginClient.assertProtocolState(ProtocolState.LOGIN);

        loginClient.setProtocolState(ProtocolState.LOGGING_IN); // make sure only one packet

        LoginServer.getInstance().getExecutor().execute(() -> {
            try {
                this.handleLogin(loginClient);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private enum RateLimitResponse {
        OK, ERROR, RATE_LIMIT
    }

    private static RateLimitResponse rateLimit(String key, int time, int max) {
        try (Jedis jedis = LoginServer.getInstance().getRedisManager().getResource()) {

            String rateLimitRaw = jedis.get(key);

            if (rateLimitRaw != null) {
                int num = Integer.parseInt(rateLimitRaw);
                if (num > max) {
                    return RateLimitResponse.RATE_LIMIT;
                }
            }

            jedis.incrBy(key, 1);
            jedis.expire(key, time);

            return RateLimitResponse.OK;
        } catch (Exception e) {
            e.printStackTrace();
            return RateLimitResponse.ERROR;
        }
    }

    private static Document lookup(String token) {
        // try to lookup with token
        if (token != null) {
            return LoginServer.getInstance().getLoginMongoManager().getPlayers().find(new Document("token", token)).first();
        }
        return null;
    }

    private void handleLogin(LoginClient client) {
        String ip = ((InetSocketAddress) client.getChannel().remoteAddress()).getAddress().toString().split("/")[1];
        LoginServer.log("Handle login " + ip);

        // rate limiting IP to 15 logins per 1 min
        RateLimitResponse response = rateLimit("login:" + ip, 60, 14);
        if (response == RateLimitResponse.RATE_LIMIT) {
            client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, RATE_LIMIT_TEXT));
            return;
        }

        if (response == RateLimitResponse.ERROR) {
            client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, SERVER_ERROR));
            return;
        }

        Document document;

        try {
            document = lookup(this.token);
        } catch (Exception e) {
            e.printStackTrace();
            client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, SERVER_ERROR));
            return;
        }

        boolean saveToken = false;

        if (document == null) {
            // create new
            saveToken = true;

            // rate limit account creation to 10 per 10min hour (in case someone makes accounts in public)
            response = rateLimit("accountCreation:" + ip, 600, 10);
            if (response == RateLimitResponse.RATE_LIMIT) {
                client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, RATE_LIMIT_TEXT));
                return;
            }

            if (response == RateLimitResponse.ERROR) {
                client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, SERVER_ERROR));
                return;
            }

            System.out.println("creating a new account");

            int id = LoginServer.getInstance().getLoginMongoManager().generatePlayerId();
            String token = TokenGenerator.generateToken(id);
            System.out.println("Generating new account id " + id + " token " + token);

            document = new Document("_id", id);
            document.put("token", token);

            document.put("create_ip", ip);

            // TODO: save device information

            document.put("location", 0); // TODO: find

            // just so server knows what version playerdata was created on in case of updates
            document.put("version", 1);

            try {
                InsertOneResult result = LoginServer.getInstance().getLoginMongoManager().getPlayers().insertOne(document);
            } catch (MongoWriteException e) {
                System.out.println("CANT insert " + document);
                e.printStackTrace();
                client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, SERVER_ERROR));
                return;
            }

        } else {
            // ban reason
            if (document.containsKey("banned")) {
                client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.BANNED, "You are permanently banned for unset reason"));
                return;
            }
        }

        int playerId = document.getInteger("_id");

        LoginServer.log("player logged in id " + playerId);

        if (saveToken) {
            String token = document.getString("token");
            client.sendPacket(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.SAVE_TOKEN, token));
        }

        MasterConnection connection = LoginServer.getInstance().getRedisTask().getMasterConnection(client.getVersion());
        if (connection == null || !connection.isConnected()) {
            client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, SERVER_ERROR));
            return;
        }

        int location = document.getInteger("location");
        // TODO make sure location still exists
        if (false) {
            location = 0;
        }

        connection.sendPacketWithResponse(new LoginMasterOutgoingPacket(playerId, client.getKey(), location), new MasterConnection.RequestResponse() {
            @Override
            public void onAccept(WaitForResponseIncomingPacket res) {
                LoginMasterIncomingPacket packet = (LoginMasterIncomingPacket) res;

                String secret = packet.getSecret();

                System.out.println("accept " + secret);

                if (packet.getResponse() == LoginMasterIncomingPacket.LoginResponse.OK) {

                    LoginOutgoingPacket.ServerInfo serverInfo = new LoginOutgoingPacket.ServerInfo(packet.getServerType(), packet.getHost(), packet.getPort());

                    String playerIdHash = PlayerId.convertIdToHash(playerId);

                    System.out.println("LOGIN OK");
                    client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.OK, secret, playerIdHash, serverInfo));
                } else {
                    LoginServer.warn("Login failed on master for " + secret);

                    client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, SERVER_ERROR));
                }
            }

            @Override
            public void onTimeout() {
                client.sendPacketAndClose(new LoginOutgoingPacket(LoginOutgoingPacket.LoginResponse.FAIL, SERVER_ERROR));
            }
        });
    }
}
