package git.aatufutaa.lobby.net.confirm.packet.heropass;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BuyNextTierHeroPassOutgoingPacket implements OutgoingPacket {

    private final int id;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.id);
    }
}
