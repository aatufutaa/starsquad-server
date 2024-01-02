package git.aatufutaa.game.game.block;

import git.aatufutaa.game.game.map.GameMap;

public class BlockWater extends Block{

    public BlockWater(int x, int y) {
        super(BlockType.WATER, x, y);
    }

    @Override
    public void addCollision(GameMap.BlockCollisionCreator creator) {
        creator.addCollision(new BlockCollision(this.x, this.y, false));
    }
}
