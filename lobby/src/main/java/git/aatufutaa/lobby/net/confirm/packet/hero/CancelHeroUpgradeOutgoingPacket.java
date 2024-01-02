package git.aatufutaa.lobby.net.confirm.packet.hero;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CancelHeroUpgradeOutgoingPacket implements OutgoingPacket {

    private final boolean buy;

    @Override
    public void write(ByteBuf buf) {
        buf.writeBoolean(this.buy);
    }
}
