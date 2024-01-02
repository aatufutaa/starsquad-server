package git.aatufutaa.lobby.master.session;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PlayerDataMasterOutgoingPacket implements MasterOutgoingPacket {

    private final int playerId;

    private final List<Integer> friends;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.playerId);

        buf.writeByte(this.friends.size());
        for (int i : this.friends) {
            buf.writeInt(i);
        }
    }
}
