package git.aatufutaa.game.master.packet;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.game.entity.Player;
import git.aatufutaa.game.game.games.LHSGame;
import git.aatufutaa.game.game.games.tutorial.TutorialGame;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class StartGameMasterIncomingPacket implements MasterIncomingPacket {

    public enum GameType {
        TUTORIAL,
        //TOWER_WARS,
        //CANDY_RUSH,
        LAST_HERO_STANDING
    }

    private int gameId;
    private GameType gameType;

    private List<GameSession> players;

    public static class GameSession {
        private int playerId;
        private String secret;
        private byte[] key;
        private int team;

        public void read(ByteBuf buf) throws Exception {
            this.playerId = buf.readInt();
            this.secret = ByteBufUtil.readString(buf, 32);
            this.key = new byte[16];
            buf.readBytes(this.key);
            this.team = buf.readByte();
        }
    }

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.gameId = buf.readInt();
        this.gameType = GameType.values()[buf.readByte()];

        int playerCount = buf.readByte();
        this.players = new ArrayList<>(playerCount);
        for (int i = 0; i < playerCount; i++) {
            GameSession session = new GameSession();
            session.read(buf);
            this.players.add(session);
        }
    }

    @Override
    public void handle() {
        GameServer.getInstance().runOnMainThread(() -> {
            try {
                GameServer.log("Starting " + this.gameType + " game");

                switch (this.gameType) {
                    case TUTORIAL -> this.startTutorial();
                    case LAST_HERO_STANDING -> this.startLHS();
                }

                GameServer.getInstance().getMasterConnection().sendPacket(new StartGameMasterOutgoingPacket(this.gameId, StartGameMasterOutgoingPacket.StartType.OK));

            } catch (Exception e) {
                e.printStackTrace();

                GameServer.getInstance().getGameManager().cancelGame(this.gameId);

                for (GameSession session : this.players) {
                    GameServer.getInstance().getSessionManager().removeSession(session.playerId);
                }

                GameServer.warn("Game failed to start id " + this.gameId + " " + this.gameType);
                GameServer.getInstance().getMasterConnection().sendPacket(new StartGameMasterOutgoingPacket(this.gameId, StartGameMasterOutgoingPacket.StartType.FAIL));
            }
        });
    }

    private void startTutorial() {
        GameSession gameSession = this.players.get(0);

        Session session = new Session(gameSession.playerId, gameSession.secret, "name1");
        session.setKey(gameSession.key);
        GameServer.getInstance().getSessionManager().addSession(session);

        TutorialGame game = new TutorialGame(this.gameId);
        Player player = game.addPlayer(session.getName(), 2000, gameSession.team);
        game.registerPlayer(session, player);
        game.initPlayer();

        Player p1 = game.addPlayer("bot_1", 200, 1);
        Player p2 = game.addPlayer("bot_2", 200, 1);
        Player p3 = game.addPlayer("bot_2", 200, 1);

        p1.setDead();
        p2.setDead();
        p3.setDead();

        GameServer.getInstance().getGameManager().startGame(game);

        session.setGame(game);
    }

    private void startLHS() {
        LHSGame game = new LHSGame(this.gameId);
        this.startTeamGame(game);
    }

    private void startTeamGame(Game game) {
        for (GameSession gameSession : this.players) {
            Session session = new Session(gameSession.playerId, gameSession.secret, "name1");
            session.setKey(gameSession.key);
            GameServer.getInstance().getSessionManager().addSession(session);

            Player player = game.addPlayer(session.getName(), 2000, gameSession.team);
            game.registerPlayer(session, player);

            session.setGame(game);
        }

        game.fillWithBots();

        GameServer.getInstance().getGameManager().startGame(game);
    }
}
