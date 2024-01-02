package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.lobby.master.misc.UpdateFriendStatusMasterIncomingPacket;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateFriendOutgoingPacket implements OutgoingPacket {

    @AllArgsConstructor
    public static class FriendStatusUpdate {
        private final String playerId;
        private final UpdateFriendStatusMasterIncomingPacket.FriendStatus status;

        public void write(ByteBuf buf) {
            ByteBufUtil.writeString(this.playerId, buf);
            buf.writeByte(this.status.ordinal());
        }
    }

    private FriendStatusUpdate[] updates;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.updates.length);
        for (FriendStatusUpdate update : this.updates) {
            update.write(buf);
        }
    }
}
