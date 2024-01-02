package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.misc.RemoveInviteMasterOutgoingPacket;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import redis.clients.jedis.Jedis;

public class CancelFriendInviteIncomingPacket extends LobbyPacket {

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

        // check if player has sent a friend invite
        if (!session.getLobbyData().getOutgoingInvites().containsKey(playerId)) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.NOT_INVITED));
            return;
        }

        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {

                if (!FriendsLock.lock(jedis, session.getPlayerId(), playerId)) {
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.FAILED_TO_CANCEL));
                    return;
                }

                // remove invite from friend in redis
                if (jedis.srem("incoming_invites_" + playerId, "" + session.getPlayerId()) != 1) {
                    FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                    System.out.println("player has not invited");
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.NOT_INVITED));
                    return;
                }

                // remove invite from player in redis
                jedis.srem("outgoing_invites_" + session.getPlayerId(), "" + playerId);

                String friendIdHash = PlayerId.convertIdToHash(playerId);

                FriendsLock.unlock(jedis, session.getPlayerId(), playerId);

                // remove invite from player locally
                session.getLobbyData().getOutgoingInvites().remove(playerId);
                session.sendConfirmPacket(new RemoveFriendInviteOutgoingPacket(friendIdHash, false));

                // tell master to remove invite from friend
                LobbyServer.getInstance().getMasterConnection().sendPacket(new RemoveInviteMasterOutgoingPacket(playerId, session.getPlayerId()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
