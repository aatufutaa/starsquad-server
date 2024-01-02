package git.aatufutaa.master.session;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.ServerLocation;

import java.util.function.Consumer;

public class SessionManager {

    public static final int THREADS = 4;

    private final SessionThread[] threads = new SessionThread[THREADS];

    public SessionManager() {
        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i] = new SessionThread(i);
        }
    }

    public void start() {
        for (SessionThread thread : this.threads) {
            thread.start();
        }
    }

    public void stop() {
        for (SessionThread thread : this.threads) {
            thread.stop();
        }
    }

    public void handleLogin(int playerId, byte[] key, ServerLocation location, Consumer<Session> callback) {
        SessionThread thread = this.threads[getThread(playerId)];
        thread.run(() -> {
            Session session = thread.getSession(playerId);
            if (session == null) {
                session = new Session(playerId, location);
                session.generateToken();
                thread.addSession(session);
            } else {
                session.setTimeout(0); // reset timeout so player doesnt timeout right after login
            }
            session.setKey(key);
            callback.accept(session);
        });
    }

    public void setConnected(int playerId, boolean connected) {
        SessionThread thread = this.threads[getThread(playerId)];
        thread.run(() -> {
            Session session = thread.getSession(playerId);
            if (session == null) {
                return;
            }
            session.setConnected(connected);
        });
    }

    public void runOnSessionThread(int playerId, Consumer<Void> callback) {
        SessionThread thread = this.threads[getThread(playerId)];
        thread.run(() -> {
            callback.accept(null);
        });
    }

    public void tryToDisconnect(int playerId, PlayServer server) {
        SessionThread thread = this.threads[getThread(playerId)];
        thread.run(() -> {
            Session session = thread.getSession(playerId);
            if (session != null) {
                if (session.getServer() == server) {
                    MasterServer.getInstance().getServerManager(server.getServerLocation()).runOnServerThread(() -> server.removePlayer(session));
                    thread.removeSession(playerId);
                }
            }
        });
    }

    public void getSession(int playerId, Consumer<Session> callback) {
        SessionThread thread = this.threads[getThread(playerId)];
        thread.run(() -> {
            Session session = thread.getSession(playerId);
            callback.accept(session);
        });
    }

    public SessionThread getSessionThread(int thread) {
        return this.threads[thread];
    }

    public static int getThread(int playerId) {
        return playerId % THREADS;
    }
}
