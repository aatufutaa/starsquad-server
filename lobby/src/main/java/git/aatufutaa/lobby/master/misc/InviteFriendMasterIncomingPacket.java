package git.aatufutaa.lobby.master.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerDataCache;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.confirm.packet.friend.AddFriendInviteOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import redis.clients.jedis.Jedis;

public class InviteFriendMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private int friendId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.friendId = buf.readInt();
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {
            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) return;

            LobbyServer.getInstance().getAsyncThread().execute(() -> {
                try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {
                    PlayerDataCache.CacheResult res = PlayerDataCache.fetchData(jedis, this.friendId, false, false);

                    if (res.error() != PlayerDataCache.CacheError.OK) {
                        LobbyServer.warn("failed to fetch data for " + this.friendId + " invite to " + this.playerId);
                        return;
                    }

                    Document document = res.document();

                    String playerIdHash = PlayerId.convertIdToHash(this.friendId);
                    String name = document.getString("name");
                    int rating = document.getInteger("total_rating");

                    AddFriendMasterIncomingPacket.Friend friend = new AddFriendMasterIncomingPacket.Friend(playerIdHash, name, rating);

                    LobbyServer.getInstance().runOnMainThread(() -> {
                        session.sendConfirmPacket(new AddFriendInviteOutgoingPacket(friend, true));
                        session.getLobbyData().getIncomingInvites().put(this.friendId, friend);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
