package git.aatufutaa.login;

import git.aatufutaa.login.master.LoginMasterPackets;
import git.aatufutaa.login.mongo.LoginMongoManager;
import git.aatufutaa.login.net.LoginConnectionHandler;
import git.aatufutaa.login.net.LoginPackets;
import git.aatufutaa.login.net.encryption.RsaEncryption;
import git.aatufutaa.login.redis.RedisTask;
import git.aatufutaa.server.Server;
import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class LoginServer extends Server {

    @Getter
    private static LoginServer instance;

    @Getter
    private final LoginMongoManager loginMongoManager;

    @Getter
    private final RedisTask redisTask;

    @Getter
    private final ScheduledExecutorService executor;

    public LoginServer() {
        instance = this;

        this.loginMongoManager = new LoginMongoManager();

        this.redisTask = new RedisTask();

        this.executor = Executors.newScheduledThreadPool(8);
    }

    @Override
    public void start() throws Exception {
        log("Starting login server...");

        RsaEncryption.init();

        LoginPackets.register();
        super.start();

        // connect to mongo
        try {
            this.loginMongoManager.init();
        } catch (Exception e) {
            e.printStackTrace();
            this.crash("cant connect to mongo");
            return;
        }
        log("Connected to mongo");

        LoginMasterPackets.register();
        this.redisTask.start();
    }

    @Override
    public void stop(){
        log("Stopping login server...");

        super.stop();

        this.redisTask.stop();
    }

    @Override
    public ServerType getServerType() {
        return ServerType.LOGIN;
    }

    @Override
    protected Class<? extends ConnectionHandlerBase> getConnectionHandlerClass() {
        return LoginConnectionHandler.class;
    }

    @Override
    public int getVersion() {
        return this.redisTask.getCurrentVersionMajor();
    }

    @Override
    public void onMaintenanceEnabled(String s) {
    }
}
