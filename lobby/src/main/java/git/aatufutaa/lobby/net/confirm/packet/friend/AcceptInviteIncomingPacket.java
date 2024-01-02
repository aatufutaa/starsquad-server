package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.misc.AddFriendMasterIncomingPacket;
import git.aatufutaa.lobby.master.misc.AddFriendMasterOutgoingPacket;
import git.aatufutaa.lobby.master.misc.RemoveInviteMasterOutgoingPacket;
import git.aatufutaa.lobby.master.misc.UpdateFriendStatusMasterIncomingPacket;
import git.aatufutaa.lobby.mongo.PlayerDataCache;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import redis.clients.jedis.Jedis;

public class AcceptInviteIncomingPacket extends LobbyPacket {

    private String friendId;
    private boolean accepted;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.friendId = ByteBufUtil.readString(buf, 9);
        this.accepted = buf.readBoolean();
    }

    @Override
    protected void handle0(Session session) throws Exception {
        int playerId;
        try {
            playerId = PlayerId.parsePlayerId(this.friendId);
        } catch (Exception e) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.INVALID_PLAYER_ID));
            return;
        }

        // check if player has a friend invite
        if (!session.getLobbyData().getIncomingInvites().containsKey(playerId)) {
            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.NOT_INVITED));
            return;
        }

        if (this.accepted) {
            if (session.getLobbyData().getFriends().size() >= 50) {
                session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.TOO_MANY_FRIENDS));
                return;
            }
        }

        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {

                if (!FriendsLock.lock(jedis, session.getPlayerId(), playerId)) {
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.FAILED_TO_ACCEPT));
                    return;
                }

                // remove invite from player in redis
                if (jedis.srem("incoming_invites_" + session.getPlayerId(), "" + playerId) != 1) {
                    FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                    session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.NOT_INVITED));
                    return;
                }

                // remove invite from friend in redis
                jedis.srem("outgoing_invites_" + playerId, "" + session.getPlayerId());

                String friendIdHash = PlayerId.convertIdToHash(playerId);

                AddFriendMasterIncomingPacket.Friend friend;

                if (this.accepted) {

                    // get before in case its already cached
                    PlayerDataCache.CacheResult res = PlayerDataCache.fetchData(jedis, playerId, false, false);
                    if (res.error() == PlayerDataCache.CacheError.OK) {
                        Document document = res.document();

                        if (document.containsKey("friends") && document.getList("friends", Document.class).size() >= 50) {
                            FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                            LobbyServer.getInstance().runOnMainThread(() -> {
                                session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.OTHER_TOO_MANY_FRIENDS));
                            });
                            return;
                        }

                        String friendName = document.getString("name");
                        int rating = document.getInteger("total_rating");
                        friend = new AddFriendMasterIncomingPacket.Friend(friendIdHash, friendName, rating);
                    } else {
                        FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                        LobbyServer.getInstance().runOnMainThread(() -> {
                            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.FAILED_TO_ACCEPT));
                        });
                        return;
                    }

                    res = PlayerDataCache.fetchData(jedis, session.getPlayerId(), false, false);
                    if (res.error() == PlayerDataCache.CacheError.OK) {
                        Document document = res.document();

                        if (document.containsKey("friends") && document.getList("friends", Document.class).size() >= 50) {
                            FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                            LobbyServer.getInstance().runOnMainThread(() -> {
                                session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.TOO_MANY_FRIENDS));
                            });
                            return;
                        }
                    } else {
                        FriendsLock.unlock(jedis, session.getPlayerId(), playerId);
                        LobbyServer.getInstance().runOnMainThread(() -> {
                            session.sendConfirmPacket(new FriendResponseOutgoingPacket(FriendResponseOutgoingPacket.FriendResponse.FAILED_TO_ACCEPT));
                        });
                        return;
                    }

                    long time = System.currentTimeMillis();
                    Document playerObject = new Document("player_id", session.getPlayerId());
                    playerObject.put("accept_at", time);
                    Document friendObject = new Document("player_id", playerId);
                    friendObject.put("accept_at", time);

                    // update friend list in mongo
                    LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                            Updates.addToSet("friends", friendObject));
                    PlayerDataCache.clearCache(jedis, session.getPlayerId());
                    LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", playerId),
                            Updates.addToSet("friends", playerObject));
                    PlayerDataCache.clearCache(jedis, playerId);
                } else {
                    friend = null;
                }

                FriendsLock.unlock(jedis, session.getPlayerId(), playerId);

                LobbyServer.getInstance().runOnMainThread(() -> {
                    // remove friend invite locally
                    session.getLobbyData().getIncomingInvites().remove(playerId);
                    session.sendConfirmPacket(new RemoveFriendInviteOutgoingPacket(friendIdHash, true));
                    if (this.accepted) {
                        if (friend != null) {
                            // send add friend locally
                            session.getLobbyData().getFriends().put(playerId, friend);
                            session.sendConfirmPacket(new AddFriendOutgoingPacket(friend, UpdateFriendStatusMasterIncomingPacket.FriendStatus.OFFLINE));
                        }
                    }
                });

                if (this.accepted) {
                    // tell master to add friend to friend
                    LobbyServer.getInstance().getMasterConnection().sendPacket(new AddFriendMasterOutgoingPacket(playerId, session.getPlayerId()));
                }
                // tell master to remove invite from friend
                LobbyServer.getInstance().getMasterConnection().sendPacket(new RemoveInviteMasterOutgoingPacket(playerId, session.getPlayerId()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
