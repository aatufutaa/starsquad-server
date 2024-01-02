package git.aatufutaa.lobby.net.packet.queue;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class QueueStatusOutgoingPacket implements OutgoingPacket {

    private final int players;
    private final int maxPlayers;
    private final boolean party;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.players);
        buf.writeByte(this.maxPlayers);
        buf.writeBoolean(this.party);
    }
}
