package git.aatufutaa.lobby.net.confirm.packet.friend;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FriendResponseOutgoingPacket implements OutgoingPacket {

    public enum FriendResponse {
        INVALID_PLAYER_ID,

        ALREADY_FRIENDS,
        ALREADY_INVITED,
        OTHER_ALREADY_INVITED,
        CANT_INVITE_SELF,
        TOO_MANY_INVITES,
        OTHER_TOO_MANY_INVITES,
        TOO_MANY_FRIENDS,
        OTHER_TOO_MANY_FRIENDS,
        FAILED_TO_INVITE,
        NOT_ALLOWING_FRIEND,

        NOT_INVITED,
        FAILED_TO_ACCEPT,

        NOT_FRIEND,
        FAILED_TO_REMOVE,

        FAILED_TO_CANCEL
    }

    private FriendResponse response;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
    }
}
