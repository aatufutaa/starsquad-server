package git.aatufutaa.master.server.lobby;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.party.Party;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.ServerType;
import git.aatufutaa.master.server.lobby.packet.misc.UpdateFriendStatusOutgoingPacket;
import git.aatufutaa.master.server.lobby.packet.session.CreateSessionOutgoingPacket;
import git.aatufutaa.master.server.lobby.packet.session.DestroySessionOutgoingPacket;
import git.aatufutaa.master.session.Session;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class LobbyServer extends PlayServer {

    public LobbyServer(int serverId, ServerLocation location, Channel channel) {
        super(serverId, location, channel);
    }

    @Override
    public ServerType getServerType() {
        return ServerType.LOBBY;
    }

    @Override
    public String toString() {
        return "LobbyServer{" + super.toString() + "}";
    }

    @Setter
    public static class SessionData {
        private int playerId;
        private String secret;
        private byte[] key;

        private boolean inQueue; // if server crashes and player is in q tell the new server we are in queue

        private boolean inParty;
        private int partyId;
        private int partyLeaderId;

        private boolean sendFriends;

        private List<UpdateFriendStatusOutgoingPacket.FriendStatusUpdate> friendStatusUpdateList;

        public SessionData(Session session) {
            this.playerId = session.getPlayerId();
            this.secret = session.getToken();
            this.key = session.getKey();

            this.inQueue = session.getQueueData() != null;

            Party party = session.getParty();
            if (party != null) {
                this.inParty = true;
                this.partyId = party.getPartyId();
                this.partyLeaderId = party.getLeader().getPlayerId();
            }
            this.sendFriends = !session.isFriendsRequested();

            if (!this.sendFriends) {
                this.friendStatusUpdateList = new ArrayList<>(session.getOnlineFriends().size());
                for (Session friend : session.getOnlineFriends().values()){
                    this.friendStatusUpdateList.add(new UpdateFriendStatusOutgoingPacket.FriendStatusUpdate(friend.getPlayerId(), friend.getStatus()));
                }
            }
        }

        public void write(ByteBuf buf) {
            buf.writeInt(this.playerId);
            ByteBufUtil.writeString(this.secret, buf);
            buf.writeBytes(this.key);

            buf.writeBoolean(this.inQueue);

            buf.writeBoolean(this.inParty);
            if (this.inParty) {
                buf.writeInt(this.partyId);
                buf.writeInt(this.partyLeaderId);
            }
            buf.writeBoolean(this.sendFriends);
            if (!this.sendFriends) {
                buf.writeByte(this.friendStatusUpdateList.size());
                for (UpdateFriendStatusOutgoingPacket.FriendStatusUpdate update : this.friendStatusUpdateList) {
                    update.write(buf);
                }
            }
        }
    }

    public boolean addToLobby(Session session, SessionData sessionData) {
        if (this.addPlayer(session)) {
            this.sendPacket(new CreateSessionOutgoingPacket(sessionData));
            return true;
        }
        return false;
    }

    @Override
    public boolean removePlayer(Session session) {
        boolean removed = super.removePlayer(session);
        if (removed)
            this.sendPacket(new DestroySessionOutgoingPacket(session.getPlayerId()));
        return removed;
    }
}
