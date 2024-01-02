package git.aatufutaa.game.game.hero;

import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.entity.Player;

public abstract class Hero {

    protected final Game game;
    protected final Player player;

    public Hero(Game game, Player player) {
        this.game = game;
        this.player = player;
    }

    public abstract void tick();
    public abstract void attack(float x, float y);
}
