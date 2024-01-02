package git.aatufutaa.master.server.login.packet;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.ServerType;
import git.aatufutaa.master.server.game.GameServer;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.server.lobby.packet.session.UpdateSessionOutgoingPacket;
import git.aatufutaa.master.server.login.LoginServer;
import io.netty.buffer.ByteBuf;

public class LoginIncomingPacket extends RequestIncomingPacket<LoginServer> {

    private int playerId;
    private byte[] key;
    private int location;

    @Override
    public void read(ByteBuf buf) throws Exception {
        super.read(buf);
        this.playerId = buf.readInt();
        this.key = new byte[16];
        buf.readBytes(this.key);
        this.location = buf.readByte();
    }

    @Override
    public void handle(LoginServer server) {

        MasterServer.log("Handle login packet");

        ServerLocation location = ServerLocation.values()[this.location];

        MasterServer.getInstance().getSessionManager().handleLogin(this.playerId, this.key, location, session -> {

            // ->WE ARE ON session player thread

            MasterServer.log("Handle login done");

            if (session.getServer() == null /*|| !session.getServer().isConnected()*/) { // if not in a server or server went down

                MasterServer.log("Need server for player");

                LobbyServer.SessionData sessionData = new LobbyServer.SessionData(session);

                MasterServer.getInstance().getServerManager(/*session.getLocation()*/ServerLocation.EU).getLobbyWithLeastPlayers(lobby -> {

                    // ->WE ARE ON server thread

                    MasterServer.log("Found server " + lobby + " for player");

                    if (lobby == null) {
                        server.sendPacket(new LoginOutgoingPacket(this.requestId, "Cant find lobby."));
                        return;
                    }

                    if (!lobby.addToLobby(session, sessionData)) {
                        MasterServer.warn("failed to add player to lobby on login " + this.playerId);
                        server.sendPacket(new LoginOutgoingPacket(this.requestId, "Server error 0x3."));
                        return;
                    }

                    MasterServer.getInstance().getSessionManager().runOnSessionThread(session.getPlayerId(), (callback) -> {

                        // ->WE ARE ON session player thread

                        session.setServer(lobby);

                        // makes sure session didnt expire during this thread change
                        if (session.isDestroyed()) {
                            MasterServer.warn("session was removed during login process for " + this.playerId);
                            MasterServer.getInstance().getServerManager(lobby.getServerLocation()).runOnServerThread(() -> lobby.removePlayer(session));
                            return;
                        }

                        server.sendPacket(new LoginOutgoingPacket(this.requestId, session.getToken(), ServerType.LOBBY, lobby.getHost(), lobby.getPort()));
                    });
                });

            } else {

                MasterServer.log("Found old server for " + this.playerId + " from " + session.getServer());

                // player is already on a server
                PlayServer currentServer = session.getServer();

                if (currentServer instanceof GameServer) {
                }

                // key got updated arleady now send to server
                // this kicks old player and updates key to new one
                currentServer.sendPacket(new UpdateSessionOutgoingPacket(session.getPlayerId(), session.getKey()));

                // send player to current server
                server.sendPacket(new LoginOutgoingPacket(this.requestId, session.getToken(), currentServer.getServerType(), currentServer.getHost(), currentServer.getPort()));
            }
        });
    }
}
