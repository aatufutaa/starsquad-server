package git.aatufutaa.server.play;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.play.session.SessionBase;
import git.aatufutaa.server.play.session.SessionManagerBase;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class PlayServer<T extends SessionBase> extends Server {

    @Getter
    private static PlayServer<?> server;

    @Getter
    protected SessionManagerBase<T> sessionManager;

    private ScheduledExecutorService mainThread;

    private int tick;

    public PlayServer() {
        server = this;
    }

    @Override
    public void start() throws Exception {
        super.start();

        this.mainThread = Executors.newSingleThreadScheduledExecutor();

        this.mainThread.scheduleAtFixedRate(() -> {
            try {
                this.tick();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("tick failed!");
                this.crash("tick failed");
            }
        }, 0L, 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        // kick players
        this.sessionManager.kickAll("Server went down!");

        super.stop();

        if (this.mainThread != null)
            this.mainThread.shutdown();
    }

    protected void tick() {
        if (++this.tick % 20 == 0) {
            this.sessionManager.tick();
        }
    }

    public void runOnMainThread(Runnable r) {
        this.mainThread.execute(() -> {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
