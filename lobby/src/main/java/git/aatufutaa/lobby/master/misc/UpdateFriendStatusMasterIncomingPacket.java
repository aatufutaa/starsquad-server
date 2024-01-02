package git.aatufutaa.lobby.master.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.confirm.packet.friend.UpdateFriendOutgoingPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

public class UpdateFriendStatusMasterIncomingPacket implements MasterIncomingPacket {

    public enum FriendStatus {
        OFFLINE,
        LOBBY,
        GAME
    }

    @Getter
    public static class FriendStatusUpdate {
        private int friendId;
        private FriendStatus status;

        public void read(ByteBuf buf) {
            this.friendId = buf.readInt();
            this.status = FriendStatus.values()[buf.readByte()];
        }
    }

    private int playerId;

    private FriendStatusUpdate[] updates;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();

        int count = buf.readByte();
        this.updates = new FriendStatusUpdate[count];
        for (int i = 0; i < count; i++) {
            FriendStatusUpdate update = new FriendStatusUpdate();
            update.read(buf);
            this.updates[i] = update;
        }
    }

    @Override
    public void handle() {
        LobbyServer.getInstance().runOnMainThread(() -> {

            System.out.println("FRIEND UPDATE " + this.playerId);

            Session session = LobbyServer.getInstance().getSessionManager().getSession(this.playerId);

            if (session == null) return;

            UpdateFriendOutgoingPacket.FriendStatusUpdate[] updates = new UpdateFriendOutgoingPacket.FriendStatusUpdate[this.updates.length];
            for (int i = 0; i < this.updates.length; i++) {
                FriendStatusUpdate update = this.updates[i];

                if (update.status == FriendStatus.OFFLINE) {
                    session.getFriendStatus().remove(update.friendId);
                } else {
                    session.getFriendStatus().put(update.friendId, update.status);
                }

                updates[i] = new UpdateFriendOutgoingPacket.FriendStatusUpdate(PlayerId.convertIdToHash(update.friendId), update.status);
            }

            session.sendConfirmPacket(new UpdateFriendOutgoingPacket(updates));
        });
    }
}
