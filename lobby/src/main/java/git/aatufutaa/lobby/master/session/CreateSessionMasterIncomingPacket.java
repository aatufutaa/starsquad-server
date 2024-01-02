package git.aatufutaa.lobby.master.session;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.misc.UpdateFriendStatusMasterIncomingPacket;
import git.aatufutaa.lobby.party.Party;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class CreateSessionMasterIncomingPacket implements MasterIncomingPacket {

    private SessionData sessionData;

    public static class SessionData {
        private int playerId;
        private String secret;
        private byte[] key;

        private boolean inQueue;

        private boolean inParty;
        private int partyId;
        private int partyLeaderId;

        private boolean requestFriends;

        private List<UpdateFriendStatusMasterIncomingPacket.FriendStatusUpdate> friendStatusUpdateList;

        public void read(ByteBuf buf) throws Exception {
            this.playerId = buf.readInt();
            this.secret = ByteBufUtil.readString(buf, 32);
            this.key = new byte[16];
            buf.readBytes(this.key);

            this.inQueue = buf.readBoolean();

            this.inParty = buf.readBoolean();
            if (this.inParty) {
                this.partyId = buf.readInt();
                this.partyLeaderId = buf.readInt();
            }
            this.requestFriends = buf.readBoolean();
            if (!this.requestFriends) {
                int count = buf.readByte();
                this.friendStatusUpdateList = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    UpdateFriendStatusMasterIncomingPacket.FriendStatusUpdate update = new UpdateFriendStatusMasterIncomingPacket.FriendStatusUpdate();
                    update.read(buf);
                    this.friendStatusUpdateList.add(update);
                }
            }
        }
    }

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.sessionData = new SessionData();
        this.sessionData.read(buf);
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {
            LobbyServer.log("create session from master -> " + this.sessionData.playerId);

            Session old = LobbyServer.getInstance().getSessionManager().removeSession(this.sessionData.playerId); // should not happen
            if (old != null) old.kick(null);

            Session session = new Session(this.sessionData.playerId, this.sessionData.secret, this.sessionData.key);

            if (this.sessionData.inQueue)
                session.setInQueue(true);

            if (this.sessionData.inParty) {
                Party party = new Party(this.sessionData.partyId, this.sessionData.partyLeaderId);
                session.setParty(party);
            }

            session.setRequestFriends(this.sessionData.requestFriends);

            if (!this.sessionData.requestFriends) {
                for (UpdateFriendStatusMasterIncomingPacket.FriendStatusUpdate update : this.sessionData.friendStatusUpdateList) {
                    session.getFriendStatus().put(update.getFriendId(), update.getStatus());
                }
            }

            LobbyServer.getInstance().getSessionManager().addSession(session);


            //LobbyServer.getInstance().getMasterConnection().sendPacket(new CreateSessionMasterOutgoingPacket(this.sessionData.playerId));
        });
    }
}
