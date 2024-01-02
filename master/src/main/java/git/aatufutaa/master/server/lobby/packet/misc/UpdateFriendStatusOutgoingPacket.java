package git.aatufutaa.master.server.lobby.packet.misc;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateFriendStatusOutgoingPacket implements OutgoingPacket {

    public enum FriendStatus {
        OFFLINE,
        LOBBY,
        GAME
    }

    @AllArgsConstructor
    public static class FriendStatusUpdate {
        private final int playerId;
        private final FriendStatus status;

        public void write(ByteBuf buf) {
            buf.writeInt(this.playerId);
            buf.writeByte(this.status.ordinal());
        }
    }

    private final int playerId;
    private final FriendStatusUpdate[] updates;

    public UpdateFriendStatusOutgoingPacket(int playerId, int friendId, FriendStatus status) {
        this.playerId = playerId;
        this.updates = new FriendStatusUpdate[1];
        this.updates[0] = new FriendStatusUpdate(friendId, status);
    }

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeByte(this.updates.length);
        for (FriendStatusUpdate update : this.updates) {
            update.write(buf);
        }
    }
}
