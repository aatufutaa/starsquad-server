package git.aatufutaa.lobby.net.confirm.packet.hero;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpgradeHeroOutgoingPacket implements OutgoingPacket {

    private final int hero;
    private final int level;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.hero);
        buf.writeByte(this.level);
    }
}
