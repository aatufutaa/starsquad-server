package git.aatufutaa.game.session;

import git.aatufutaa.game.net.udp.UdpIdGenerator;
import git.aatufutaa.server.play.session.SessionManagerBase;

import java.util.HashMap;
import java.util.Map;

public class SessionManager extends SessionManagerBase<Session> {

    private final Map<Short, Session> udp = new HashMap<>();

    @Override
    public Session removeSession(int playerId) {
        Session session = super.removeSession(playerId);
        if (session != null) {
            this.releaseUdpId(session);
        }
        return session;
    }

    private void releaseUdpId(Session session) {
        if (!session.isHasUdpId()) return;
        short oldId = session.getUdpId();
        UdpIdGenerator.release(oldId);
        this.udp.remove(oldId);
    }

    public void registerUdpId(Session session) {
        this.releaseUdpId(session);

        short udpId = UdpIdGenerator.poll();
        session.setUdpId(udpId);
        session.setHasUdpId(true);

        this.udp.put(udpId, session);
    }

    public Session getUdpSession(short udpId) {
        return this.udp.get(udpId);
    }
}
