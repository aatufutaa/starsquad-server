package git.aatufutaa.lobby.net.confirm.packet.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerDataCache;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import redis.clients.jedis.Jedis;

public class RequestProfileIncomingPacket extends LobbyPacket {

    private String playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = ByteBufUtil.readString(buf, 9);
    }

    @Override
    protected void handle0(Session session) throws Exception {
        LobbyServer.getInstance().getAsyncThread().execute(() -> {

            int playerId;
            try {
                playerId = PlayerId.parsePlayerId(this.playerId);
            } catch (Exception e) {
                System.out.println("invalid player id");
                return;
            }

            try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {
                PlayerDataCache.CacheResult res = PlayerDataCache.fetchData(jedis, playerId, false, false);

                if (res.error() != PlayerDataCache.CacheError.OK) {
                    System.out.println("failed to lookup player");
                    // TODO: send packet
                    return;
                }

                Document document = res.document();

                String name = document.getString("name");
                int rating = document.getInteger("total_rating");

                RequestProfileOutgoingPacket packet = new RequestProfileOutgoingPacket(name, rating);

                LobbyServer.getInstance().runOnMainThread(() -> {
                    session.sendConfirmPacket(packet);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
