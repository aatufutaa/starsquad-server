package git.aatufutaa.login.redis;

import git.aatufutaa.login.LoginServer;
import git.aatufutaa.server.communication.MasterConnection;
import lombok.Getter;
import org.bson.Document;

public class RedisTask {

    // current version
    @Getter
    private int currentVersionMajor;
    @Getter
    private int currentVersionMinor;

    @Getter
    private String currentAssetUrl;
    @Getter
    private String currentIosAssetVersion;
    @Getter
    private String currentAndroidAssetVersion;

    @Getter
    private String downloadLinkIos;
    @Getter
    private String downloadLinkAndroid;

    private String currentMasterHost;
    private int currentMasterPort;
    private final MasterConnection currentMasterConnection = new MasterConnection();

    // beta
    @Getter
    private int betaVersionMajor;
    @Getter
    private int betaVersionMinor;

    @Getter
    private String betaAssetUrl;
    @Getter
    private String betaIosAssetVersion;
    @Getter
    private String betaAndroidAssetVersion;

    @Getter
    private String betaAccessKey;

    private String betaMasterHost;
    private int betaMasterPort;
    private final MasterConnection betaMasterConnection = new MasterConnection();

    private boolean dataLoaded;

    private boolean running;

    public void start() {
        this.currentMasterConnection.start();
        this.betaMasterConnection.start();

        this.running = true;
        new Thread(this::start0).start();
    }

    public void stop() {
        this.running = false;
        this.currentMasterConnection.stop();
        this.betaMasterConnection.stop();
    }

    private void start0() {
        while (this.running) {
            try {

                // get latest update from mongo
                Document update = LoginServer.getInstance().getMongoManager().getUpdates().find(new Document("_id", "main")).first();
                if (update == null) {
                    throw new Exception("update not set");
                }

                // current
                this.currentVersionMajor = update.getInteger("currentVersionMajor");
                this.currentVersionMinor = update.getInteger("currentVersionMinor");

                this.currentAssetUrl = update.getString("currentAssetUrl");

                this.currentIosAssetVersion = update.getString("currentIosAssetVersion");
                this.currentAndroidAssetVersion = update.getString("currentAndroidAssetVersion");

                this.downloadLinkIos = update.getString("downloadLinkIos");
                this.downloadLinkAndroid = update.getString("downloadLinkAndroid");

                // master connection current
                String old = this.currentMasterHost;
                int oldPort = this.currentMasterPort;
                this.currentMasterHost = update.getString("currentMasterHost");
                this.currentMasterPort = update.getInteger("currentMasterPort");
                if (old != null && !old.equals(this.currentMasterHost) || oldPort != this.currentMasterPort) {
                    this.currentMasterConnection.disconnect();
                }

                // beta
                this.betaVersionMajor = update.getInteger("betaVersionMajor");
                this.betaVersionMinor = update.getInteger("betaVersionMinor");

                this.betaAssetUrl = update.getString("betaAssetUrl");

                this.betaIosAssetVersion = update.getString("betaIosAssetVersion");
                this.betaAndroidAssetVersion = update.getString("betaAndroidAssetVersion");

                this.betaAccessKey = update.getString("betaAccessKey");

                // master connection beta
                boolean betaMasterEnabled = update.getBoolean("betaMasterEnabled");
                if (betaMasterEnabled) {
                    old = this.betaMasterHost;
                    oldPort = this.betaMasterPort;
                    this.betaMasterHost = update.getString("betaMasterHost");
                    this.betaMasterPort = update.getInteger("betaMasterPort");
                    if (old != null && !old.equals(this.betaMasterHost) || oldPort != this.betaMasterPort) {
                        this.betaMasterConnection.disconnect();
                    }
                } else {
                    this.betaMasterConnection.disconnect();
                }

                this.dataLoaded = true;

                if (!this.currentMasterConnection.isConnected()) {
                    try {
                        this.currentMasterConnection.connect(this.currentMasterHost, this.currentMasterPort, new LoginMasterListener());
                    } catch (Exception e) {
                        LoginServer.warn("cant connect to master (current)");
                    }
                }

                if (betaMasterEnabled && !this.betaMasterConnection.isConnected()) {
                    try {
                        this.betaMasterConnection.connect(this.betaMasterHost, this.betaMasterPort, new LoginMasterListener());
                    } catch (Exception e) {
                        LoginServer.warn("cant connect to master (beta)");
                    }
                }

            } catch (Exception e) {
                this.dataLoaded = false;
                e.printStackTrace();
                LoginServer.warn("Cant get version data from mongo!!!");
            }

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
        }
    }

    public MasterConnection getMasterConnection(int version) {
        if (version == this.currentVersionMajor) return this.currentMasterConnection;
        if (version == this.betaVersionMajor) return this.betaMasterConnection;
        return null;
    }

    public boolean isRedisDown() {
        return !this.dataLoaded;
    }

    public boolean isCurrentMasterDown() {
        return !this.dataLoaded || !this.currentMasterConnection.isConnected();
    }

    public boolean isBetaMasterDown() {
        return !this.dataLoaded || !this.currentMasterConnection.isConnected();
    }
}
