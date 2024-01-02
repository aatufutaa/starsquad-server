package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.misc.AddFriendMasterIncomingPacket;
import git.aatufutaa.lobby.master.misc.InviteFriendMasterOutgoingPacket;
import git.aatufutaa.lobby.mongo.PlayerDataCache;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.List;

public class InviteFriendIncomingPacket extends LobbyPacket {

    private String friendId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.friendId = ByteBufUtil.readString(buf, 9);
    }

    @Override
    protected void handle0(Session session) throws Exception {
        // TODO: rate linimt

        int playerId;
        try {
            playerId = PlayerId.parsePlayerId(this.friendId);
        } catch (Exception e) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.INVALID_PLAYER_ID));
            return;
        }

        // check if self
        if (playerId == session.getPlayerId()) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.CANT_INVITE_SELF));
            return;
        }

        // check if already friends
        if (session.getLobbyData().getFriends().containsKey(playerId)) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.ALREADY_FRIENDS));
            return;
        }

        // check if player has already invited friend
        if (session.getLobbyData().getOutgoingInvites().containsKey(playerId)) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.ALREADY_INVITED));
            return;
        }

        // check if friend has already invited this player
        if (session.getLobbyData().getIncomingInvites().containsKey(playerId)) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.OTHER_ALREADY_INVITED));
            return;
        }

        // too many invites
        if (session.getLobbyData().getOutgoingInvites().size() >= 50) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.TOO_MANY_INVITES));
            return;
        }

        LobbyServer.getInstance().getAsyncThread().execute(() -> {

            try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {

                if (!FriendsLock.lock(jedis, session.getPlayerId(), playerId)) {
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.FAILED_TO_INVITE));
                    return;
                }

                // look up friend data
                PlayerDataCache.CacheResult res = PlayerDataCache.fetchData(jedis, playerId, true, false);
                if (res.error() == PlayerDataCache.CacheError.PLAY_NOT_FOUND) {
                    FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.INVALID_PLAYER_ID));
                    return;
                }

                if (res.error() != PlayerDataCache.CacheError.OK) {
                    FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.FAILED_TO_INVITE));
                    return;
                }

                // if player has too many invites
                if (jedis.scard("outgoing_invites_" + session.getPlayerId()) >= 50) {
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.TOO_MANY_INVITES));
                    return;
                }

                // if friend has too many
                if (jedis.scard("incoming_invites_" + playerId) >= 50) {
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.OTHER_TOO_MANY_INVITES));
                    return;
                }

                // check if player has already invited with redis
                if (jedis.sismember("incoming_invites_" + session.getPlayerId(), "" + playerId)) {
                    FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.OTHER_ALREADY_INVITED));
                    return;
                }

                // check if already friends with mongo
                Document document = res.document();
                if (document.containsKey("friends")) {
                    List<Document> list = document.getList("friends", Document.class);

                    for (Document friend : list) {
                        if (friend.getInteger("player_id") == session.getPlayerId()) {
                            FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.ALREADY_FRIENDS));
                            return;
                        }
                    }
                }

                if (!document.getBoolean("allow_friend_requests", true)) {
                    FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.NOT_ALLOWING_FRIEND));
                    return;
                }

                // check if friend has already invited this player with redis
                if (jedis.sadd("outgoing_invites_" + session.getPlayerId(), "" + playerId) != 1) {
                    FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.ALREADY_INVITED));
                    return;
                }

                jedis.sadd("incoming_invites_" + playerId, "" + session.getPlayerId());

                FriendsLock.unlock(jedis, session.getPlayerId(), playerId);

                String friendPlayerIdHash = PlayerId.convertIdToHash(playerId);
                String name = document.getString("name");
                int rating = document.getInteger("total_rating");

                AddFriendMasterIncomingPacket.Friend friend = new AddFriendMasterIncomingPacket.Friend(friendPlayerIdHash, name, rating);

                // add friend to player locally
                LobbyServer.getInstance().runOnMainThread(() -> {
                    session.getLobbyData().getOutgoingInvites().put(playerId, friend);
                    session.sendConfirmPacket(new AddFriendInviteOutgoingPacket(friend, false));
                });

                // tell master to send friend invite to friend if connected
                LobbyServer.getInstance().getMasterConnection().sendPacket(new InviteFriendMasterOutgoingPacket(session.getPlayerId(), playerId));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
