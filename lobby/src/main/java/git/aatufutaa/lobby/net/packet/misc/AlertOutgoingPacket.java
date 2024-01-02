package git.aatufutaa.lobby.net.packet.misc;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AlertOutgoingPacket implements OutgoingPacket {

    public enum AlertType {
        FAILED_TO_START_GAME
    }

    private final AlertType alertType;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.alertType.ordinal());
    }
}
