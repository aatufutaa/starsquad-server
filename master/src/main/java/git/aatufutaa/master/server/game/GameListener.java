package git.aatufutaa.master.server.game;

import lombok.Getter;
import lombok.Setter;

public abstract class GameListener {

    @Getter
    @Setter
    private GameState gameState = GameState.IDLE;

    public abstract void onStarted();

    public abstract void onCancel();
}
