package git.aatufutaa.server.maintenance;

import git.aatufutaa.server.Server;
import lombok.Getter;
import org.bson.Document;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Maintenance {

    @Getter
    private boolean enabled;
    @Getter
    private String msg;

    private ScheduledExecutorService thread;

    public Maintenance() {
    }

    public void start() {
        this.thread = Executors.newSingleThreadScheduledExecutor();
        this.thread.scheduleAtFixedRate(this::tick, 2L, 2L, TimeUnit.SECONDS);
    }

    public void stop() {
        this.thread.shutdown();
    }

    private void tick() {
        try {
            Document document = Server.getServer().getMongoManager().getUpdates().find(new Document("_id", "maintenance_" + Server.getServer().getVersion())).first();

            if (document == null) {
                if (this.enabled) {
                    Server.log("Maintenance mode has been disabled");
                    this.enabled = false;
                }
            } else {
                if (!this.enabled) {
                    Server.log("Maintenance mode has been enabled!");
                    this.enabled = true;
                    this.msg = document.getString("msg");

                    Server.getServer().onMaintenanceEnabled(this.msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Server.warn("failed to get maintenance data");
        }
    }
}
