package git.aatufutaa.game.net.packet;

import git.aatufutaa.game.game.GameState;
import git.aatufutaa.game.game.entity.EntityData;
import git.aatufutaa.game.game.games.tutorial.TutorialDynamicGameData;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TickOutgoingPacket implements OutgoingPacket {

    private final int serverTick;
    private final List<TickPacket> packets;
    private final List<EntityData> fallbackData;

    public interface TickPacket {
        int getId();

        void write(ByteBuf buf);
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShortLE(this.serverTick);
        buf.writeByte(this.packets.size());
        for (TickPacket packet : this.packets) {
            buf.writeByte(packet.getId());
            packet.write(buf);
        }
        if (this.fallbackData != null) {
            buf.writeByte(this.fallbackData.size());
            for (EntityData entityData : this.fallbackData) {
                entityData.write(buf);
            }
        } else {
            buf.writeByte(0);
        }
    }

    @AllArgsConstructor
    public static class AddProjectilePacket implements TickPacket {

        private final int entityId;
        private final int projectileId;

        private final float x;
        private final float y;

        private final float velX;
        private final float velY;

        @Override
        public int getId() {
            return 0;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.entityId);
            buf.writeByte(this.projectileId);

            buf.writeFloatLE(this.x);
            buf.writeFloatLE(this.y);

            buf.writeFloatLE(this.velX);
            buf.writeFloatLE(this.velY);
        }
    }

    @AllArgsConstructor
    public static class RemoveProjectilePacket implements TickPacket {

        private final int entityId;
        private final int projectileId;

        private final float x;
        private final float y;

        @Override
        public int getId() {
            return 1;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.entityId);
            buf.writeByte(this.projectileId);

            buf.writeFloatLE(this.x);
            buf.writeFloatLE(this.y);
        }
    }

    @AllArgsConstructor
    public static class BlockHitPacket implements TickPacket {

        private final int x;
        private final int y;

        private final float velX;
        private final float velY;

        @Override
        public int getId() {
            return 2;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.x);
            buf.writeByte(this.y);

            buf.writeFloatLE(this.velX);
            buf.writeFloatLE(this.velY);
        }
    }

    @AllArgsConstructor
    public static class DamageEntityPacket implements TickPacket {

        private final int entityId;
        private final int health;

        @Override
        public int getId() {
            return 3;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.entityId);
            buf.writeShortLE(this.health);
        }
    }

    @AllArgsConstructor
    public static class RespawnPlayerPacket implements TickPacket {

        private final int entityId;

        private final float x;
        private final float y;

        @Override
        public int getId() {
            return 4;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.entityId);

            buf.writeFloatLE(this.x);
            buf.writeFloatLE(this.y);
        }
    }

    @AllArgsConstructor
    public static class TutorialPacket implements TickPacket {

        private final TutorialDynamicGameData dynamicGameData;

        @Override
        public int getId() {
            return 5;
        }

        @Override
        public void write(ByteBuf buf) {
            this.dynamicGameData.write(buf);
        }
    }

    @AllArgsConstructor
    public static class GameStatePacket implements TickPacket {

        private final GameState gameState;

        @Override
        public int getId() {
            return 6;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.gameState.ordinal());
        }
    }

    @AllArgsConstructor
    public static class GameStartingPacket implements TickPacket {

        private int seconds;

        @Override
        public int getId() {
            return 7;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.seconds);
        }
    }

    @AllArgsConstructor
    public static class GameWinnerPacket implements TickPacket {

        private List<Integer> winners;

        @Override
        public int getId() {
            return 8;
        }

        @Override
        public void write(ByteBuf buf) {
            buf.writeByte(this.winners.size());
            for (int i : this.winners) {
                buf.writeByte(i);
            }
        }
    }

    public static class YouDiedPacket implements TickPacket {

        @Override
        public int getId() {
            return 9;
        }

        @Override
        public void write(ByteBuf buf) {
        }
    }
}
