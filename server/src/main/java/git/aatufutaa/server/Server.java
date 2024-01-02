package git.aatufutaa.server;

import git.aatufutaa.server.maintenance.Maintenance;
import git.aatufutaa.server.mongo.MongoManager;
import git.aatufutaa.server.net.NetworkManager;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import git.aatufutaa.server.redis.RedisManager;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public abstract class Server {

    @Getter
    private static Server server;

    private static Logger logger;

    @Getter
    protected String host;
    @Getter
    private int port;

    @Getter
    private ServerLocation location;

    private final NetworkManager networkManager;

    @Getter
    private final RedisManager redisManager;

    @Getter
    private final MongoManager mongoManager;

    @Getter
    private final Maintenance maintenance;

    public Server() {
        server = this;
        logger = LogManager.getLogger(this.getClass());

        this.networkManager = new NetworkManager(this.getConnectionHandlerClass());

        this.redisManager = new RedisManager();

        this.mongoManager = new MongoManager();

        this.maintenance = new Maintenance();
    }

    public void start() throws Exception {
        File file = new File("server.properties");

        if (!file.exists()) {
            try {
                file.createNewFile();

                URL inputUrl = getClass().getResource("/server.properties");
                FileUtils.copyURLToFile(inputUrl, file);

                System.out.println("Server properties was created! Change the host and port and restart the server!");
                System.exit(0);
                return;

            } catch (IOException e) {
                e.printStackTrace();
                this.crash("failed to read server data");
                return;
            }
        }

        System.out.println("Reading server properties from " + file.getAbsolutePath());
        try (InputStream input = new FileInputStream(file)) {

            Properties properties = new Properties();
            properties.load(input);

            this.host = properties.getProperty("host");
            this.port = Integer.parseInt(properties.getProperty("port"));
            this.location = ServerLocation.getByName(properties.getProperty("location"));

            if (this.location == null) {
                throw new Exception("cant find location " + properties.getProperty("location"));
            }

            System.out.println("host " + this.host);
            System.out.println("port " + this.port);
            System.out.println("location " + this.location);
        } catch (IOException e) {
            e.printStackTrace();
            this.crash("failed to read server data");
        }

        this.networkManager.start(this.host, this.port);

        this.redisManager.start();

        this.mongoManager.init();

        this.maintenance.start();
    }

    public void stop() {
        this.redisManager.close();

        this.mongoManager.close();

        this.maintenance.stop();
    }

    public void crash(String reason) {
        warn("Crashing server for " + reason);

        try {
            this.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }

        System.exit(-1);
    }

    public abstract ServerType getServerType();

    protected abstract Class<? extends ConnectionHandlerBase> getConnectionHandlerClass();

    public abstract int getVersion();

    public abstract void onMaintenanceEnabled(String msg);

    public static void log(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warn(msg);
    }
}
