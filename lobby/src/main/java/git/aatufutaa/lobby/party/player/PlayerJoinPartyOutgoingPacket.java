package git.aatufutaa.lobby.party.player;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerJoinPartyOutgoingPacket implements OutgoingPacket {

    @AllArgsConstructor
    public static class PartyMember {
        private final String playerId;
        private final String name;

        private int trophies;

        private final int hero;
        private int heroTrophies;

        public void write(ByteBuf buf) {
            ByteBufUtil.writeString(this.playerId, buf);
            ByteBufUtil.writeString(this.name, buf);

            buf.writeIntLE(this.trophies);

            buf.writeByte(this.hero);
            buf.writeShortLE(this.heroTrophies);
        }
    }

    private PartyMember partyMember;

    @Override
    public void write(ByteBuf buf) {
        this.partyMember.write(buf);
    }
}
