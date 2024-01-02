package git.aatufutaa.master.server.lobby.packet;

import git.aatufutaa.master.communication.packet.PingPacketManager;
import git.aatufutaa.master.server.lobby.packet.game.SendToGameOutgoingPacket;
import git.aatufutaa.master.server.lobby.packet.queue.JoinQueueIncomingPacket;
import git.aatufutaa.master.server.lobby.packet.queue.LeaveQueueIncomingPacket;
import git.aatufutaa.master.server.lobby.packet.queue.LeaveQueueOutgoingPacket;
import git.aatufutaa.master.server.lobby.packet.queue.QueueStatusOutgoingPacket;
import git.aatufutaa.master.server.lobby.packet.tutorial.StartTutorialIncomingPacket;
import git.aatufutaa.master.server.lobby.packet.tutorial.StartTutorialOutgoingPacket;
import git.aatufutaa.master.party.*;
import git.aatufutaa.master.server.lobby.packet.misc.*;
import git.aatufutaa.master.server.lobby.packet.session.*;

public class LobbyPacketManager extends PingPacketManager {

    public static final LobbyPacketManager MANAGER = new LobbyPacketManager();

    public LobbyPacketManager() {
        this.registerIncoming(2, CreateSessionIncomingPacket.class);
        this.registerOutgoing(2, CreateSessionOutgoingPacket.class);
        this.registerOutgoing(3, DestroySessionOutgoingPacket.class);
        this.registerIncoming(4, PlayerOnlineIncomingPacket.class);
        this.registerOutgoing(5, UpdateSessionOutgoingPacket.class);
        this.registerIncoming(6, DisconnectPlayerIncomingPacket.class);

        this.registerIncoming(7, StartTutorialIncomingPacket.class);
        this.registerOutgoing(7, StartTutorialOutgoingPacket.class);

        this.registerIncoming(8, CreatePartyIncomingPacket.class);
        this.registerOutgoing(8, CreatePartyOutgoingPacket.class);
        this.registerIncoming(9, LeavePartyIncomingPacket.class);
        this.registerOutgoing(9, LeavePartyOutgoingPacket.class);
        this.registerIncoming(10, JoinPartyIncomingPacket.class);
        this.registerOutgoing(10, JoinPartyOutgoingPacket.class);

        this.registerIncoming(11, JoinQueueIncomingPacket.class);
        this.registerOutgoing(11, QueueStatusOutgoingPacket.class);
        this.registerIncoming(12, LeaveQueueIncomingPacket.class);
        this.registerOutgoing(12, LeaveQueueOutgoingPacket.class);

        this.registerOutgoing(13, SendToGameOutgoingPacket.class);

        this.registerOutgoing(14, PlayerJoinPartyOutgoingPacket.class);
        this.registerOutgoing(15, PlayerLeavePartyOutgoingPacket.class);

        this.registerIncoming(16, PlayerDataIncomingPacket.class);

        // friend
        this.registerIncoming(17, AddFriendIncomingPacket.class);
        this.registerOutgoing(17, AddFriendOutgoingPacket.class);

        this.registerIncoming(18, RemoveFriendIncomingPacket.class);
        this.registerOutgoing(18, RemoveFriendOutgoingPacket.class);

        this.registerIncoming(19, InviteFriendIncomingPacket.class);
        this.registerOutgoing(19, InviteFriendOutgoingPacket.class);

        this.registerIncoming(20, RemoveInviteIncomingPacket.class);
        this.registerOutgoing(20, RemoveInviteOutgoingPacket.class);

        this.registerOutgoing(21, UpdateFriendStatusOutgoingPacket.class);

        this.registerIncoming(22, UpdateLocationIncomingPacket.class);
    }
}
