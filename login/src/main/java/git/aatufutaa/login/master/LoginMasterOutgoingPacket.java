package git.aatufutaa.login.master;

import git.aatufutaa.server.communication.packet.packets.WaitForResponseMasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginMasterOutgoingPacket extends WaitForResponseMasterOutgoingPacket {

    private int playerId;
    private byte[] key;
    private int location;

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        buf.writeInt(this.playerId);
        buf.writeBytes(this.key);
        buf.writeByte(this.location);
    }
}
