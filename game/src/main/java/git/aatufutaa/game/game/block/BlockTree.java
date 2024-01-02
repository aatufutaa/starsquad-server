package git.aatufutaa.game.game.block;

import git.aatufutaa.game.game.map.GameMap;

public class BlockTree extends BlockDestroyable {

    public BlockTree(int x, int y) {
        super(BlockType.TREE, x, y);
    }

    @Override
    public void addCollision(GameMap.BlockCollisionCreator creator) {
        BlockCollision parent = new BlockCollision(this.x, this.y);
        creator.addCollision(parent);
        creator.addCollision(new BlockCollision(this.x + 1, this.y, parent));
        creator.addCollision(new BlockCollision(this.x, this.y + 1, parent));
        creator.addCollision(new BlockCollision(this.x + 1, this.y + 1, parent));
    }
}
