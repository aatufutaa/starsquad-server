package git.aatufutaa.server.play.session;

import git.aatufutaa.server.Server;

import java.util.HashMap;
import java.util.Map;

public class SessionManagerBase<T extends SessionBase> {

    private final Map<Integer, T> sessions = new HashMap<>();
    private final Map<String, T> secrets = new HashMap<>();

    public void addSession(T session) {
        this.sessions.put(session.getPlayerId(), session);
        this.secrets.put(session.getSecret(), session);
    }

    public T removeSession(int playerId) {
        T session = this.sessions.remove(playerId);
        if (session != null) {
            this.secrets.remove(session.getSecret());
        }
        return session;
    }

    public T getSession(int playerId) {
        return this.sessions.get(playerId);
    }

    public T getSessionBySecret(String secret) {
        return this.secrets.get(secret);
    }

    public void kickAll(String msg) {
        for (T session : this.secrets.values()) {
            session.kick(msg);
        }
    }

    public void tick() {
        for (T session : this.sessions.values()) {
            try {
                session.tick();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("failed to tick " + session);
                Server.getServer().crash("failed to tick session " + session);
            }
        }
    }
}
