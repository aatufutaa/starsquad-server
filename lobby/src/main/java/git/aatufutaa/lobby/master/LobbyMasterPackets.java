package git.aatufutaa.lobby.master;

import git.aatufutaa.lobby.master.game.SendToGameMasterIncomingPacket;
import git.aatufutaa.lobby.master.misc.*;
import git.aatufutaa.lobby.master.queue.JoinQueueMasterOutgoingPacket;
import git.aatufutaa.lobby.master.queue.LeaveQueueMasterIncomingPacket;
import git.aatufutaa.lobby.master.queue.LeaveQueueMasterOutgoingPacket;
import git.aatufutaa.lobby.master.queue.QueueStatusMasterIncomingPacket;
import git.aatufutaa.lobby.master.session.*;
import git.aatufutaa.lobby.master.tutorial.StartTutorialMasterIncomingPacket;
import git.aatufutaa.lobby.master.tutorial.StartTutorialMasterOutgoingPacket;
import git.aatufutaa.lobby.party.master.*;
import git.aatufutaa.server.communication.packet.MasterPacketManager;

public class LobbyMasterPackets {

    public static void register() {
        MasterPacketManager.registerIncoming(2, CreateSessionMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(2, CreateSessionMasterOutgoingPacket.class);
        MasterPacketManager.registerIncoming(3, DestroySessionIncomingPacket.class);
        MasterPacketManager.registerOutgoing(4, PlayerOnlineMasterOutgoingPacket.class);
        MasterPacketManager.registerIncoming(5, UpdateSessionMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(6, DisconnectMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(7, StartTutorialMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(7, StartTutorialMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(8, CreatePartyMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(8, CreatePartyMasterOutgoingPacket.class);
        MasterPacketManager.registerIncoming(9, LeavePartyMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(9, LeavePartyMasterOutgoingPacket.class);
        MasterPacketManager.registerIncoming(10, JoinPartyMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(10, JoinPartyMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(11, QueueStatusMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(11, JoinQueueMasterOutgoingPacket.class);
        MasterPacketManager.registerIncoming(12, LeaveQueueMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(12, LeaveQueueMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(13, SendToGameMasterIncomingPacket.class);

        MasterPacketManager.registerIncoming(14, PlayerJoinPartyMasterIncomingPacket.class);
        MasterPacketManager.registerIncoming(15, PlayerLeavePartyMasterIncomingPacket.class);

        MasterPacketManager.registerOutgoing(16, PlayerDataMasterOutgoingPacket.class);

        // friend
        MasterPacketManager.registerIncoming(17, AddFriendMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(17, AddFriendMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(18, RemoveFriendMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(18, RemoveFriendMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(19, InviteFriendMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(19, InviteFriendMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(20, RemoveInviteMasterIncomingPacket.class);
        MasterPacketManager.registerOutgoing(20, RemoveInviteMasterOutgoingPacket.class);

        MasterPacketManager.registerIncoming(21, UpdateFriendStatusMasterIncomingPacket.class);

        MasterPacketManager.registerOutgoing(22, UpdateLocationMasterOutgoingPacket.class);
    }
}
