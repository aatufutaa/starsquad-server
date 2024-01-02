package git.aatufutaa.game.game.map;

import git.aatufutaa.game.game.block.Block;
import git.aatufutaa.game.game.block.BlockCollision;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMap {

    private final String theme;
    private final int sizeX;
    private final int sizeY;
    private final Block[] blocks;

    private Map<Integer, SpawnPoint> spawnPointMap = new HashMap<>();

    @Getter
    @AllArgsConstructor
    public class SpawnPoint {
        private int x;
        private int y;
    }

    public GameMap(String theme, int sizeX, int sizeY) {
        this.theme = theme;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.blocks = new Block[sizeX * sizeY];
    }

    public void addSpawnPoint(int team, int x, int y) {
        this.spawnPointMap.put(team, new SpawnPoint(x, y));
    }

    public SpawnPoint getSpawnPoint(int team) {
        return this.spawnPointMap.get(team);
    }

    private int getKey(int x, int y) {
        return (y + this.sizeY / 2) * this.sizeX + (x + this.sizeX / 2);
    }

    public void addBlock(Block block) {
        this.blocks[this.getKey(block.getX(), block.getY())] = block;
    }

    public GameMapImage createImage() {
        List<Block> blocks = new ArrayList<>();

        BlockCollisionCreator collisionCreator = new BlockCollisionCreator(this.sizeX, this.sizeY);

        for (Block block : this.blocks) {
            if (block == null) continue;
            blocks.add(block);
            block.addCollision(collisionCreator);
        }

        return new GameMapImage(this, this.theme, this.sizeX, this.sizeY, blocks, collisionCreator.collisions);
    }

    public class BlockCollisionCreator {

        private final BlockCollision[] collisions;

        public BlockCollisionCreator(int sizeX, int sizeY) {
            this.collisions = new BlockCollision[sizeX * sizeY];
        }

        public void addCollision(BlockCollision collision) {
            this.collisions[GameMap.this.getKey(collision.getX(), collision.getY())] = collision;
        }
    }
}
