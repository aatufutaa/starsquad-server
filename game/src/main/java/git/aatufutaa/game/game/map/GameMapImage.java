package git.aatufutaa.game.game.map;

import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.game.game.block.Block;
import git.aatufutaa.game.game.block.BlockCollision;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class GameMapImage {

    // static
    private final String theme;
    @Getter
    private final int sizeX;
    @Getter
    private final int sizeY;
    private final List<Block> blocks;

    // dynamic
    private final BlockCollision[] collisions;
    private final List<Block> dirtyBlocks = new ArrayList<>();

    @Getter
    private final GameMap handle;

    public GameMapImage(GameMap gameMap, String theme, int sizeX, int sizeY, List<Block> blocks, BlockCollision[] collisions) {
        this.theme = theme;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.blocks = blocks;
        this.collisions = collisions;
        this.handle = gameMap;
    }

    private int getKey(int x, int y) {
        return (y + this.sizeY / 2) * this.sizeX + (x + this.sizeX / 2);
    }

    public void addCollision(int x, int y, BlockCollision collision) {
        this.collisions[this.getKey(x, y)] = collision;
    }

    public BlockCollision getCollision(int x, int y) {
        return this.collisions[this.getKey(x, y)];
    }

    public void flagDirty(Block block) {
        if (block.isDirty()) return;
        block.setDirty(true);
        this.dirtyBlocks.add(block);
    }

    public void removeCollision(int x, int y) {
        this.addCollision(x, y, null);
    }

    public void writeStatic(ByteBuf buf) {
        ByteBufUtil.writeString(this.theme, buf);
        buf.writeByte(this.sizeX);
        buf.writeByte(this.sizeY);
        buf.writeShortLE(this.blocks.size());
        for (Block block : this.blocks) {
            block.writeStatic(buf);
        }
    }

    public DynamicMapData getDynamicData() {
        return new DynamicMapData(this.dirtyBlocks);
    }
}
