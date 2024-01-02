package git.aatufutaa.master;

import git.aatufutaa.master.command.CommandManager;
import git.aatufutaa.master.communication.NetworkManager;
import git.aatufutaa.master.party.PartyManager;
import git.aatufutaa.master.queue.QueueManager;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.ServerManager;
import git.aatufutaa.master.session.SessionManager;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MasterServer {

    @Getter
    private static MasterServer instance;

    private static final Logger logger = LogManager.getLogger(MasterServer.class);

    private final NetworkManager networkManager;

    @Getter
    private final ServerManager[] serverManagers;

    @Getter
    private final SessionManager sessionManager;

    @Getter
    private final QueueManager queueManager;

    @Getter
    private final PartyManager partyManager;

    private final CommandManager commandManager;

    public MasterServer() {
        instance = this;

        this.networkManager = new NetworkManager();
        this.sessionManager = new SessionManager();
        this.serverManagers = new ServerManager[ServerLocation.values().length];
        for (int i = 0; i < this.serverManagers.length; i++) {
            this.serverManagers[i] = new ServerManager();
        }
        this.queueManager = new QueueManager();
        this.partyManager = new PartyManager();
        this.commandManager = new CommandManager();
    }

    public void start() throws Exception {
        log("Starting master server...");

        try {
            this.networkManager.start(1234);
            for (ServerManager serverManager : this.serverManagers) {
                serverManager.start();
            }
            this.sessionManager.start();
            this.queueManager.start();
            this.partyManager.start();
            this.commandManager.start();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                this.stop();
            } catch ( Exception ex) {
                ex.printStackTrace();
            }
            System.exit(-1);
        }
    }

    public void stop() {
        log("Stopping master server...");

        this.networkManager.stop();
        for (ServerManager serverManager : this.serverManagers) {
            serverManager.stop();
        }
        this.sessionManager.stop();
        this.queueManager.stop();
        this.partyManager.stop();
        this.commandManager.stop();
    }

    public ServerManager getServerManager(ServerLocation serverLocation) {
        return this.serverManagers[serverLocation.ordinal()];
    }

    public static void log(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warn(msg);
    }
}
