package git.aatufutaa.game.game.block;

import git.aatufutaa.game.game.map.GameMap;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

public class Block {

    private final BlockType blockType;
    @Getter
    protected final int x;
    @Getter
    protected final int y;

    @Getter
    @Setter
    private boolean dirty;

    public Block(BlockType blockType, int x, int y ) {
        this.blockType = blockType;
        this.x = x;
        this.y = y;
    }

    public void writeStatic(ByteBuf buf) {
        buf.writeByte(this.blockType.ordinal());
        buf.writeByte(this.x);
        buf.writeByte(this.y);
    }

    /*public void writeDynamic(ByteBuf buf) {
        buf.writeByte(this.x);
        buf.writeByte(this.y);
    }*/

    public void addCollision(GameMap.BlockCollisionCreator creator) {
        creator.addCollision(new BlockCollision(this.x, this.y));
    }
}
