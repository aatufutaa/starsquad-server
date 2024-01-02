package git.aatufutaa.master.server.game.packet;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.game.GameServer;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.buffer.ByteBuf;

public class HomeIncomingPacket implements IncomingPacket<GameServer> {

    private int playerId;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.playerId = buf.readInt();
    }

    @Override
    public void handle(GameServer server) {
        MasterServer.getInstance().getSessionManager().getSession(this.playerId, session -> {

            if (session == null) {
                MasterServer.warn("cant find session when home for " + this.playerId);
                return;
            }

            if (session.getServer() != server) {
                MasterServer.warn("player server changed somehow when home " + this.playerId);
                return;
            }

            LobbyServer.SessionData sessionData = new LobbyServer.SessionData(session); // get session data

            // get lobby
            MasterServer.getInstance().getServerManager(session.getLocation()).getLobbyWithLeastPlayers(lobby -> {

                if (lobby == null) {
                    MasterServer.warn("cant find available lobby when home " + this.playerId);
                    // TODO: send fail?
                    return;
                }

                if (!lobby.addToLobby(session, sessionData)) {
                    MasterServer.warn("failed to add player to lobby on home " + this.playerId);
                    return;
                }

                MasterServer.getInstance().getSessionManager().runOnSessionThread(this.playerId, (callback) -> {

                    if (session.getServer() != server) {
                        MasterServer.warn("player server changed when home " + this.playerId);
                        MasterServer.getInstance().getServerManager(lobby.getServerLocation()).runOnServerThread(() -> lobby.removePlayer(session));
                        return;
                    }

                    session.setServer(lobby); // add to new server
                    server.sendPacket(new HomeOutgoingPacket(this.playerId, lobby.getHost(), lobby.getPort()));

                    // TODO: this does not remove sessio
                    MasterServer.getInstance().getServerManager(server.getServerLocation()).runOnServerThread(() -> server.removePlayer(session)); // remove from game
                });
            });
        });
    }
}
