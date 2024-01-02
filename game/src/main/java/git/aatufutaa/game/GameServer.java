package git.aatufutaa.game;

import git.aatufutaa.game.game.GameManager;
import git.aatufutaa.game.master.GameMasterListener;
import git.aatufutaa.game.master.packet.GameMasterPackets;
import git.aatufutaa.game.mongo.GameMongoManager;
import git.aatufutaa.game.net.GameConnectionHandler;
import git.aatufutaa.game.net.packet.GamePackets;
import git.aatufutaa.game.net.udp.UdpNetworkManager;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.game.session.SessionManager;
import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.MasterConnection;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import git.aatufutaa.server.play.PlayServer;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer extends PlayServer<Session> {

    public static final int MASTER_VERSION = 1;
    private static final String MASTER_HOST = "localhost";
    private static final int MASTER_PORT = 1234;

    @Getter
    private static GameServer instance;

    @Getter
    private final MasterConnection masterConnection;

    @Getter
    private final GameManager gameManager;

    @Getter
    private final UdpNetworkManager udpNetworkManager;

    @Getter
    private ExecutorService threadPool;

    @Getter
    private final GameMongoManager gameMongoManager;

    public GameServer() {
        instance = this;

        this.masterConnection = new MasterConnection();

        this.gameManager = new GameManager();

        this.sessionManager = new SessionManager();

        this.udpNetworkManager = new UdpNetworkManager();

        this.gameMongoManager = new GameMongoManager();
    }

    @Override
    public void start() throws Exception {
        log("Starting game server...");

        GamePackets.register();
        super.start();

        GameMasterPackets.register();
        try {
            this.masterConnection.start();
            this.masterConnection.connect(MASTER_HOST, MASTER_PORT, new GameMasterListener());
        } catch (Exception e) {
            this.crash("Cant connect to master");
            return;
        }

        this.threadPool = Executors.newFixedThreadPool(8);

        this.udpNetworkManager.start();

        this.gameMongoManager.init();
    }

    @Override
    public void stop() {
        log("Stopping game server...");

        super.stop();

        this.masterConnection.stop();

        if (this.threadPool != null)
            this.threadPool.shutdown();

        this.udpNetworkManager.stop();
    }

    @Override
    public ServerType getServerType() {
        return ServerType.GAME;
    }

    @Override
    protected Class<? extends ConnectionHandlerBase> getConnectionHandlerClass() {
        return GameConnectionHandler.class;
    }

    @Override
    public int getVersion() {
        return MASTER_VERSION;
    }

    @Override
    public void onMaintenanceEnabled(String s) {
    }

    @Override
    protected void tick() {
        super.tick();
        this.gameManager.tick();
    }
}
