package git.aatufutaa.lobby;

import git.aatufutaa.lobby.level.PlayerLevelManager;
import git.aatufutaa.lobby.master.LobbyMasterListener;
import git.aatufutaa.lobby.master.LobbyMasterPackets;
import git.aatufutaa.lobby.mongo.LobbyMongoManager;
import git.aatufutaa.lobby.net.LobbyConnectionHandler;
import git.aatufutaa.lobby.net.packet.LobbyPackets;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.MasterConnection;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import git.aatufutaa.server.play.PlayServer;
import git.aatufutaa.server.play.session.SessionManagerBase;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LobbyServer extends PlayServer<Session> {

    private static final String MASTER_HOST = "localhost";
    private static final int MASTER_PORT = 1234;
    public static final int MASTER_VERSION = 1;

    @Getter
    private static LobbyServer instance;

    @Getter
    private final MasterConnection masterConnection;

    @Getter
    private final LobbyMongoManager lobbyMongoManager;

    @Getter private ScheduledExecutorService asyncThread;

    @Getter
    private final PlayerLevelManager levelManager;

    public LobbyServer() {
        instance = this;

        this.masterConnection = new MasterConnection();

        this.sessionManager = new SessionManagerBase<>();

        this.lobbyMongoManager = new LobbyMongoManager();

        this.levelManager = new PlayerLevelManager();
    }

    @Override
    public void start() throws Exception {
        log("Starting lobby server...");

        LobbyPackets.register();
        super.start();

        LobbyMasterPackets.register();

        try {
            this.masterConnection.start();
            this.masterConnection.connect(MASTER_HOST, MASTER_PORT, new LobbyMasterListener());
        } catch (Exception e) {
            this.crash("Cant connect to master");
            return;
        }

        try {
            this.lobbyMongoManager.init();
        } catch (Exception e) {
            e.printStackTrace();
            this.crash("cant connect to mongo");
        }

        this.asyncThread = Executors.newScheduledThreadPool(8);

        this.levelManager.start();
    }

    @Override
    public void stop() {
        log("Stopping lobby server...");

        super.stop();

        this.masterConnection.stop();

        if (this.asyncThread != null)
            this.asyncThread.shutdown();

        this.levelManager.stop();
    }

    @Override
    public ServerType getServerType() {
        return ServerType.LOBBY;
    }

    @Override
    protected Class<? extends ConnectionHandlerBase> getConnectionHandlerClass() {
        return LobbyConnectionHandler.class;
    }

    @Override
    public int getVersion() {
        return MASTER_VERSION;
    }

    @Override
    public void onMaintenanceEnabled(String s) {

    }
}
