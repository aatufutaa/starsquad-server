package git.aatufutaa.lobby.net.confirm.packet.heropass;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClaimHeroPassRewardOutgoingPacket implements OutgoingPacket {

    private final int id;
    private final boolean hero;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.id);
        buf.writeBoolean(this.hero);
    }
}
