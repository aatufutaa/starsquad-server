package git.aatufutaa.game.game;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.master.packet.HomeMasterOutgoingPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final Map<Integer, Game> games = new HashMap<>();

    public GameManager() {
    }

    public void startGame(Game game) {
        this.games.put(game.getGameId(), game);
    }

    public void cancelGame(int gameId) {
        this.games.remove(gameId);
    }

    public void tick() {
        List<Game> removed = new ArrayList<>();

        for (Game game : this.games.values()) {
            if (game.isCancelled()) {
                removed.add(game);

                // TODO: what to do with players who died and left in last hero standing
                for (Player player : game.getPlayers().values()) {
                    GameServer.getInstance().getMasterConnection().sendPacket(new HomeMasterOutgoingPacket(player.getSession().getPlayerId()));
                }

                game.handleEnded();

                continue;
            }

            if (game.getGameState() == GameState.ENDED) {
                removed.add(game);

                game.handleEnded();

                continue;
            }

            try {
                game.tick();
            } catch (Exception e) {
                e.printStackTrace();
                GameServer.warn("Failed to tick game " + game);
                game.cancel();
            }
        }

        for (Game game : removed) {
            this.games.remove(game.getGameId());
        }
    }
}
