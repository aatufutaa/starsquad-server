package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.misc.RemoveFriendMasterOutgoingPacket;
import git.aatufutaa.lobby.mongo.PlayerDataCache;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import redis.clients.jedis.Jedis;

public class RemoveFriendIncomingPacket extends LobbyPacket {

    private String playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = ByteBufUtil.readString(buf, 9);
    }

    @Override
    protected void handle0(Session session) throws Exception {
        int playerId;
        try {
            playerId = PlayerId.parsePlayerId(this.playerId);
        } catch (Exception e) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.INVALID_PLAYER_ID));
            return;
        }

        // check if player has a friend
        if (!session.getLobbyData().getFriends().containsKey(playerId)) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.NOT_FRIEND));
            return;
        }

        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {

                if (!FriendsLock.lock(jedis, session.getPlayerId(), playerId)) {
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.FAILED_TO_REMOVE));
                    return;
                }

                String friendIdHash = PlayerId.convertIdToHash(playerId);

                Document playerObject = new Document("player_id", session.getPlayerId());
                Document friendObject = new Document("player_id", playerId);

                // remove friend from friend list in mongo
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                        Updates.pull("friends", friendObject));
                PlayerDataCache.clearCache(jedis, session.getPlayerId());
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", playerId),
                        Updates.pull("friends", playerObject));
                PlayerDataCache.clearCache(jedis, playerId);

                FriendsLock.unlock(jedis, session.getPlayerId(), playerId);

                // remove friend and send packet from player locally
                session.getLobbyData().getFriends().remove(playerId);
                session.sendConfirmPacket(new RemoveFriendOutgoingPacket(friendIdHash));

                // tell master to remove friend from friend
                LobbyServer.getInstance().getMasterConnection().sendPacket(new RemoveFriendMasterOutgoingPacket(playerId, session.getPlayerId()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
