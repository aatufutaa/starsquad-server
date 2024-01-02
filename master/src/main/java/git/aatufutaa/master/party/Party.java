package git.aatufutaa.master.party;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.server.Server;
import git.aatufutaa.master.session.Session;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Party {

    @Getter
    private final int partyId;

    private boolean destroyed;

    @Getter
    private Session leader;

    @Getter
    private List<Session> members = new ArrayList<>();

    private Set<Integer> invites = new HashSet<>();

    private final Lock lock = new ReentrantLock();

    public Party(int partyId, Session session) {
        this.partyId = partyId;
        this.leader = session;
        this.members.add(session);
    }

    public void lock(Runnable r) {
        this.lock.lock();
        try {
            r.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.lock.unlock();
    }

    public boolean isReady() {
        if (this.destroyed) return false;
        for (Session session : this.members) {
            if (false) {
                return false; // TODO: check if ready
            }
        }
        return true;
    }

    public boolean join(Session session) {
        if (this.destroyed) return false;

        if (this.members.size() >= 3) {
            return false;
        }

        this.members.add(session);

        for (Session member : this.members) {
            if (member == session) continue;
            member.sendIfLobby(new PlayerJoinPartyOutgoingPacket(member.getPlayerId(),
                   new JoinPartyOutgoingPacket.PartyMember(session)
            ));
        }

        return true;
    }

    public void leave(Session session, Server server, boolean send) {
        if (this.destroyed) return;

        if (send && (session.getQueueData() != null || session.getServer() != server)) {
            server.sendPacket(new LeavePartyOutgoingPacket(session.getPlayerId(), LeavePartyOutgoingPacket.PartyLeaveResponse.CANT_LEAVE));
            return;
        }

        if (session == this.leader) {
            this.destroy();
            return;
        }

        this.members.remove(session);

        session.setParty(null);

        if (server != null)
            server.sendPacket(new LeavePartyOutgoingPacket(session.getPlayerId(), LeavePartyOutgoingPacket.PartyLeaveResponse.OK));

        for (Session member : this.members) {
            member.sendIfLobby(new PlayerLeavePartyOutgoingPacket(member.getPlayerId(), session.getPlayerId()));
        }
    }

    private void destroy() {
        if (this.destroyed) return;

        MasterServer.getInstance().getPartyManager().destroyParty(this);

        this.destroyed = true;

        for (Session session : this.members) {
            MasterServer.getInstance().getSessionManager().runOnSessionThread(session.getPlayerId(), (callback) -> {
                session.setParty(null);

                Server server = session.getServer();
                if (server != null)
                    server.sendPacket(new LeavePartyOutgoingPacket(session.getPlayerId(), LeavePartyOutgoingPacket.PartyLeaveResponse.DISBAND));
            });
        }
    }
}
