package git.aatufutaa.game.game.map;

import git.aatufutaa.game.game.block.Block;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class DynamicMapData {

    private List<Block> dirtyBlocks;

    public void write(ByteBuf buf) {
        /*buf.writeShortLE(this.dirtyBlocks.size());
        for (Block block : this.dirtyBlocks) {
            block.writeDynamic(buf);
        }*/
    }
}
