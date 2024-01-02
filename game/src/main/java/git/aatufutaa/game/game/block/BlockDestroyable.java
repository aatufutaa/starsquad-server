package git.aatufutaa.game.game.block;

import lombok.Getter;
import lombok.Setter;

public class BlockDestroyable extends Block {

    @Getter
    @Setter
    private boolean destroyed;

    public BlockDestroyable(BlockType blockType, int x, int y) {
        super(blockType, x, y);
    }

    /*@Override
    public void writeDynamic(ByteBuf buf) {
        buf.writeBoolean(this.destroyed);
    }*/
}
