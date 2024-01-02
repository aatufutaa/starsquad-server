package git.aatufutaa.game.game.block;

import lombok.Getter;

@Getter
public class BlockCollision {

    private final int x;
    private final int y;

    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    private boolean blocksProjectiles;

    private BlockCollision parent;

    public BlockCollision(int x, int y) {
        this(x, y, true);
    }

    public BlockCollision(int x, int y, boolean blocksProjectiles) {
        this.x = x;
        this.y = y;
        this.minY = 0f;
        this.minY = 0f;
        this.maxX = 1f;
        this.maxY = 1f;
        this.blocksProjectiles = blocksProjectiles;
    }

    public BlockCollision(int x, int y, BlockCollision parent) {
        this(x, y);
        this.parent = parent;
    }
}
