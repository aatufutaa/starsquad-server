package git.aatufutaa.game.net.packet;

import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GameResultOutgoingPacket implements OutgoingPacket {

    private final int place;
    private final int kills;
    private final int giveTrophies;
    private final int giveTokens;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.place);
        buf.writeByte(this.kills);
        buf.writeByte(this.giveTrophies);
        buf.writeByte(this.giveTokens);
    }
}
