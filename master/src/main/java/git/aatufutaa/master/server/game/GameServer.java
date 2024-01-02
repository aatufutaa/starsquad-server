package git.aatufutaa.master.server.game;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.queue.GameQueue;
import git.aatufutaa.master.queue.QueueData;
import git.aatufutaa.master.queue.QueueType;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.ServerType;
import git.aatufutaa.master.server.game.packet.StartGameIncomingPacket;
import git.aatufutaa.master.server.game.packet.StartGameOutgoingPacket;
import git.aatufutaa.master.session.Session;
import io.netty.channel.Channel;

import java.util.*;

public class GameServer extends PlayServer {

    private int gameId;
    private final Map<Integer, GameListener> games = new HashMap<>();

    public GameServer(int serverId, ServerLocation location, Channel channel) {
        super(serverId, location, channel);
    }

    @Override
    public ServerType getServerType() {
        return ServerType.GAME;
    }

    @Override
    public String toString() {
        return "GameServer{" + super.toString() + "}";
    }

    public void startTutorial(Session session, GameListener callback) {
        if (!this.isConnected()) {
            callback.onCancel();
            return;
        }

        int gameId = this.gameId++;

        this.games.put(gameId, callback);

        this.sendPacket(new StartGameOutgoingPacket(gameId, StartGameOutgoingPacket.GameType.TUTORIAL,
                Collections.singletonList(new StartGameOutgoingPacket.GameSession(session, 0)))
        );
    }

    public void startTeamGame(QueueType queueType, List<GameQueue.QueueTeam> teams, GameListener callback) {
        if (!this.isConnected()) {
            callback.onCancel();
            return;
        }

        int gameId = this.gameId++;

        this.games.put(gameId, callback);

        List<StartGameOutgoingPacket.GameSession> sessions = new ArrayList<>();
        int teamIndex = 0;
        for (GameQueue.QueueTeam team : teams) {
            for (QueueData queueData : team.getPlayers()) {
                StartGameOutgoingPacket.GameSession session = new StartGameOutgoingPacket.GameSession(
                        queueData.getSession(), teamIndex
                );
                sessions.add(session);

                if (queueData.getMembers() != null) {
                    for (Session member : queueData.getMembers()) {
                        sessions.add(new StartGameOutgoingPacket.GameSession(
                                member, teamIndex
                        ));
                    }
                }
            }
            ++teamIndex;
        }

        StartGameOutgoingPacket.GameType gameType =
                switch (queueType) {
                    //case TOWER_WARS -> StartGameOutgoingPacket.GameType.TOWER_WARS;
                    //case CANDY_RUSH -> StartGameOutgoingPacket.GameType.CANDY_RUSH;
                    case LAST_HERO_STANDING -> StartGameOutgoingPacket.GameType.LAST_HERO_STANDING;
                };

        this.sendPacket(new StartGameOutgoingPacket(gameId, gameType, sessions));
    }

    public void confirm(int gameId, StartGameIncomingPacket.StartType startType) {
        GameListener listener = this.games.get(gameId);

        if (listener == null) {
            MasterServer.warn("cant confirm game " + gameId + " because it doesnt exist");
            return;
        }

        if (startType == StartGameIncomingPacket.StartType.FAIL) {
            this.games.remove(gameId);
            MasterServer.warn("Game failed to start -> " + gameId);
            listener.setGameState(GameState.FAILED);
            listener.onCancel();
            return;
        }

        if (startType == StartGameIncomingPacket.StartType.STATE_ENDED) {
            this.games.remove(gameId);
            listener.setGameState(GameState.ENDED);
            return;
        }

        if (startType == StartGameIncomingPacket.StartType.OK) {
            listener.onStarted();
            listener.setGameState(GameState.STARTING);
            return;
        }

        if (startType == StartGameIncomingPacket.StartType.STATE_PLAY) {
            listener.setGameState(GameState.STARTED);
            return;
        }

        if (startType == StartGameIncomingPacket.StartType.STATE_ENDING) {
            listener.setGameState(GameState.ENDED);
        }
    }

    @Override
    public void crash() {
        super.crash();

        for (GameListener listener : this.games.values()) {
            listener.onCancel();
        }
        this.games.clear();
    }
}
