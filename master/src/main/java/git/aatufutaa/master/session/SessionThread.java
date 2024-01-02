package git.aatufutaa.master.session;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.lobby.packet.misc.UpdateFriendStatusOutgoingPacket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionThread {

    private final int threadId;
    private ScheduledExecutorService thread;
    private final Map<Integer, Session> sessions = new HashMap<>();

    public SessionThread(int threadId) {
        this.threadId = threadId;
    }

    public void tick() {
        Iterator<Map.Entry<Integer, Session>> iterator = this.sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Session> entry = iterator.next();

            // if player is not connected and player is in lobby or not in server at all
            Session session = entry.getValue();

            try {
                if (!session.isConnected() && (session.getServer() == null || session.getServer() instanceof LobbyServer)) {
                    session.setTimeout(session.getTimeout() + 1);

                    // dont remove player if in queue however if in queue and disconnected over 5s try to leave from queue
                    if (session.getQueueData() != null) {
                        if (session.getTimeout() > 5) {
                            session.getQueueData().leave(session);
                        }
                        continue;
                    }

                    // dont remove if in party however if disconnected over 10s try to leave
                    if (session.getParty() != null) {
                        if (session.getTimeout() > 10) {
                            session.getParty().lock(() -> {
                                session.getParty().leave(session, session.getServer(), false);
                            });
                        }
                        continue;
                    }

                    if (session.getTimeout() > 30) {
                        iterator.remove(); // remove from map

                        session.setDestroyed(true);

                        // tell server to remove session
                        PlayServer server = session.getServer();
                        MasterServer.log("Sending destroy to " + server);
                        if (server != null) {
                            MasterServer.getInstance().getServerManager(server.getServerLocation()).runOnServerThread(() ->
                                    server.removePlayer(session));
                        }

                        MasterServer.warn(session + " timeout");

                        for (Session friend : session.getOnlineFriends().values()) {
                            MasterServer.getInstance().getSessionManager().runOnSessionThread(friend.getPlayerId(), (callback) -> {
                                friend.getOnlineFriends().remove(session.getPlayerId());
                                friend.sendIfLobby(new UpdateFriendStatusOutgoingPacket(friend.getPlayerId(), session.getPlayerId(), UpdateFriendStatusOutgoingPacket.FriendStatus.OFFLINE));
                            });
                        }
                    }
                } else {
                    session.setTimeout(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MasterServer.warn("failed to tick " + session);
            }
        }
    }

    public void addSession(Session session) {
        this.sessions.put(session.getPlayerId(), session);
    }

    public void removeSession(int playerId) {
        this.sessions.remove(playerId);
    }

    public void start() {
        this.thread = Executors.newSingleThreadScheduledExecutor();
        this.thread.scheduleAtFixedRate(this::tick, 1L, 1L, TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.thread != null) {
            this.thread.shutdown();
        }
    }

    public void run(Runnable r) {
        this.thread.execute(r);
    }

    public Session getSession(int playerId) {
        return this.sessions.get(playerId);
    }
}
