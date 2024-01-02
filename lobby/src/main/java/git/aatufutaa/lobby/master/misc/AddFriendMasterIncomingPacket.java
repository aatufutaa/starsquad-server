package git.aatufutaa.lobby.master.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerDataCache;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.confirm.packet.friend.AddFriendOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import org.bson.Document;
import redis.clients.jedis.Jedis;

public class AddFriendMasterIncomingPacket implements MasterIncomingPacket {

    private int playerId;
    private int friendId;
    private UpdateFriendStatusMasterIncomingPacket.FriendStatus status;

    @AllArgsConstructor
    public static class Friend {
        private final String playerId;
        private final String name;
        private final int rating;

        public void write(ByteBuf buf) {
            ByteBufUtil.writeString(this.playerId, buf);
            ByteBufUtil.writeString(this.name, buf);
            buf.writeIntLE(this.rating);
        }
    }

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
        this.friendId = buf.readInt();
        this.status = UpdateFriendStatusMasterIncomingPacket.FriendStatus.values()[buf.readByte()];
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
                        LobbyServer.warn("failed to lookup friend data for " + this.friendId);
                        return;
                    }

                    Document document = res.document();

                    String playerIdHash = PlayerId.convertIdToHash(this.friendId);
                    String name = document.getString("name");
                    int rating = document.getInteger("total_rating");

                    Friend friend = new Friend(playerIdHash, name, rating);

                    // send packet
                    LobbyServer.getInstance().runOnMainThread(() -> {
                        session.getLobbyData().getFriends().put(this.friendId, friend);
                        if (this.status != UpdateFriendStatusMasterIncomingPacket.FriendStatus.OFFLINE)
                            session.getFriendStatus().put(this.friendId, this.status);
                        session.sendConfirmPacket(new AddFriendOutgoingPacket(friend, this.status));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
