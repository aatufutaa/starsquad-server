package git.aatufutaa.master.server.lobby.packet.session;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateSessionOutgoingPacket implements OutgoingPacket {

    private int playerId;
    private byte[] key;

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeInt(this.playerId);
        buf.writeBytes(this.key);
    }
}
