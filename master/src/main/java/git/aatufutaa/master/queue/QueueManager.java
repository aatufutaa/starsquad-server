package git.aatufutaa.master.queue;

import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.session.Session;

public class QueueManager {

    private final GameQueue[][] queues;

    public QueueManager() {
        this.queues = new GameQueue[ServerLocation.values().length][QueueType.values().length];
        for (ServerLocation location : ServerLocation.values()) {
            GameQueue[] queues = this.queues[location.ordinal()];
            for (QueueType queueType : QueueType.values()) {
                queues[queueType.ordinal()] = new GameQueue(queueType, location);
            }
        }
    }

    public void start() {
        for (GameQueue[] queues : this.queues) {
            for (GameQueue queue : queues) {
                queue.start();
            }
        }
    }

    public void stop() {
        for (GameQueue[] queues : this.queues) {
            for (GameQueue queue :queues) {
                queue.stop();
            }
        }
    }

    public void add(Session session, QueueType queueType) {
        this.queues[session.getLocation().ordinal()][queueType.ordinal()].add(session);
    }
}
