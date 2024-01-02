package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.game.game.GameType;
import git.aatufutaa.game.game.map.GameMapImage;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class GameDataOutgoingPacket implements OutgoingPacket {

    private int udpPort;

    private GameType gameType;

    private int thePlayer;
    private List<PlayerInfo> players;

    private GameMapImage map;

    @Override
    public void write(ByteBuf buf) {
        buf.writeShortLE(this.udpPort);

        buf.writeByte(this.gameType.ordinal());

        buf.writeByte(this.thePlayer);

        buf.writeByte(this.players.size());
        for (PlayerInfo playerInfo : this.players)
            playerInfo.write(buf);

        this.map.writeStatic(buf);
    }

    @AllArgsConstructor
    public static class PlayerInfo {
        private int entityId;
        private String name;
        private int hero;
        private int skin;
        private boolean team;
        private int maxHealth;

        public void write(ByteBuf buf) {
            buf.writeByte(this.entityId);
            ByteBufUtil.writeString(this.name, buf);
            buf.writeByte(this.hero);
            buf.writeByte(this.skin);
            buf.writeBoolean(this.team);
            buf.writeShortLE(this.maxHealth);
        }
    }
}
