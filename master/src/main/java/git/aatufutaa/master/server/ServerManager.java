package git.aatufutaa.master.server;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.server.game.GameServer;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.server.login.LoginServer;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ServerManager {

    private final Map<Integer, Server> servers = new HashMap<>();
    private int serverId;

    private ScheduledExecutorService serverThread;
    private Thread thread;

    public ServerManager() {
    }

    public void start() {
        this.serverThread = Executors.newSingleThreadScheduledExecutor();
        this.serverThread.scheduleAtFixedRate(this::tick, 1L, 1L, TimeUnit.SECONDS);
        this.serverThread.execute(() -> this.thread = Thread.currentThread());
    }

    public void logServers() {
        for (Server server : this.servers.values()) {
            MasterServer.log("- " + server);
        }
    }

    public boolean isInServerThread() {
        return Thread.currentThread() == this.thread;
    }

    public void stop() {
        if (this.serverThread != null) {
            this.serverThread.shutdown();
        }
    }

    public void getLobbyWithLeastPlayers(Consumer<LobbyServer> callback) {
        this.serverThread.execute(() -> {
            LobbyServer lobby = null;
            int min = 0;
            for (Server server : this.servers.values()) {
                if (!(server instanceof LobbyServer)) continue;
                if (lobby == null || ((LobbyServer) server).getPlayerCount() < min) {
                    lobby = (LobbyServer) server;
                    min = ((LobbyServer) server).getPlayerCount();
                }
            }
            callback.accept(lobby);
        });
    }

    public void getGameWithLeastPlayers(Consumer<GameServer> callback) {
        this.serverThread.execute(() -> {
            GameServer lobby = null;
            int min = 0;
            for (Server server : this.servers.values()) {
                if (!(server instanceof GameServer)) continue;
                if (lobby == null || ((GameServer) server).getPlayerCount() < min) {
                    lobby = (GameServer) server;
                    min = ((GameServer) server).getPlayerCount();
                }
            }
            callback.accept(lobby);
        });
    }

    public Server addServer(ServerType serverType, ServerLocation location, Channel channel) {
        int id = this.serverId++;

        Server server;
        switch (serverType) {
            case LOGIN:
                server = new LoginServer(id, location, channel);
                break;
            case LOBBY:
                server = new LobbyServer(id, location, channel);
                break;
            case GAME:
                server = new GameServer(id, location, channel);
                break;
            default:
                return null;
        }

        this.servers.put(id, server);

        return server;
    }

    public void removeServer(Server server) {
        this.servers.remove(server.getServerId());
    }

    private void tick() {
        Iterator<Map.Entry<Integer, Server>> iterator = this.servers.entrySet().iterator();
        while (iterator.hasNext()) {

            Map.Entry<Integer, Server> entry = iterator.next();

            if (entry.getValue().isConnected()) {
                entry.getValue().tick();
                continue;
            }

            iterator.remove();

            MasterServer.warn(entry.getValue() + " crashed");
            entry.getValue().crash();
        }
    }

    public void runOnServerThread(Runnable r) {
        this.serverThread.execute(r);
    }
}
