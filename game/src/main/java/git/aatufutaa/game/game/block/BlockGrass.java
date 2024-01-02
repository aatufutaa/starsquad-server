package git.aatufutaa.game.game.block;

import git.aatufutaa.game.game.map.GameMap;

public class BlockGrass extends BlockDestroyable{

    public BlockGrass(int x, int y) {
        super(BlockType.GRASS, x, y);
    }

    @Override
    public void addCollision(GameMap.BlockCollisionCreator creator) {
    }
}
