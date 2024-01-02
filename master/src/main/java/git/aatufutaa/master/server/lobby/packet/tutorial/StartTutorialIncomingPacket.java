package git.aatufutaa.master.server.lobby.packet.tutorial;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.game.GameListener;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class StartTutorialIncomingPacket implements IncomingPacket<LobbyServer> {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle(LobbyServer server) {
        MasterServer.log("handle start tut");

        MasterServer.getInstance().getSessionManager().getSession(this.playerId, session -> {

            if (session == null) {
                MasterServer.warn("cant find session on start tuto for " + this.playerId);
                return;
            }

            if (session.isDestroyed()) return;

            MasterServer.getInstance().getServerManager(session.getLocation()).getGameWithLeastPlayers(gameServer -> {

                if (gameServer == null) {
                    MasterServer.warn("cant find game server for tuto " + this.playerId);
                    server.sendPacket(new StartTutorialOutgoingPacket(StartTutorialOutgoingPacket.TutorialState.CANT_FIND_SERVER, this.playerId));
                    MasterServer.getInstance().getSessionManager().tryToDisconnect(this.playerId, server);
                    return;
                }

                if (!gameServer.addPlayer(session)) {
                    MasterServer.warn("failed to add player to game server tut " + session);
                }

                // send start game to server
                gameServer.startTutorial(session, new GameListener() {

                    @Override
                    public void onStarted() {
                        MasterServer.log("onStarted");

                        MasterServer.getInstance().getSessionManager().runOnSessionThread(session.getPlayerId(), (p2) -> {

                            if (session.isDestroyed()) {
                                MasterServer.warn("session destroyed during start tutorial for " + session.getPlayerId());
                                // TODO: send destroy game
                                return;
                            }

                            session.setServer(gameServer);

                            MasterServer.log("game server found sending start tuto");

                            server.sendPacket(new StartTutorialOutgoingPacket(StartTutorialOutgoingPacket.TutorialState.OK, session.getPlayerId(), gameServer.getHost(), gameServer.getPort()));

                            MasterServer.getInstance().getServerManager(server.getServerLocation()).runOnServerThread(() -> server.removePlayer(session)); // remove player from lobby after
                        });
                    }

                    @Override
                    public void onCancel() {
                        gameServer.removePlayer(session);

                        MasterServer.warn("tutorial start got cancel");
                        server.sendPacket(new StartTutorialOutgoingPacket(StartTutorialOutgoingPacket.TutorialState.FAIL, session.getPlayerId()));
                        MasterServer.getInstance().getSessionManager().tryToDisconnect(session.getPlayerId(), server);
                    }
                });
            });
        });
    }
}
