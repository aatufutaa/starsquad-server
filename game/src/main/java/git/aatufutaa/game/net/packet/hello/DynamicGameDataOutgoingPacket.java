package git.aatufutaa.game.net.packet.hello;

import git.aatufutaa.game.game.DynamicGameData;
import git.aatufutaa.game.game.GameState;
import git.aatufutaa.game.game.map.DynamicMapData;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DynamicGameDataOutgoingPacket implements OutgoingPacket {

    private int currentTick;

    private GameState gameState;

    private DynamicGameData dynamicGameData;

    private List<PlayerInfo> players;

    private DynamicMapData map;

    @Override
    public void write(ByteBuf buf) {
        buf.writeShortLE(this.currentTick);

        buf.writeByte(this.gameState.ordinal());

        this.dynamicGameData.write(buf);

        buf.writeByte(this.players.size());
        for (PlayerInfo playerInfo : this.players) {
            playerInfo.write(buf);
        }
        //this.map.write(buf);
    }

    @AllArgsConstructor
    public static class PlayerInfo {

        private int entityId;
        private int health;

        private float x;
        private float y;

        public void write(ByteBuf buf) {
            buf.writeByte(this.entityId);
            buf.writeShortLE(this.health);

            buf.writeFloatLE(this.x);
            buf.writeFloatLE(this.y);
        }
    }
}
