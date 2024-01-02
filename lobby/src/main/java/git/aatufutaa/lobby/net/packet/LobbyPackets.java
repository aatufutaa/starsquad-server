package git.aatufutaa.lobby.net.packet;

import git.aatufutaa.lobby.net.confirm.LobbyConfirmPackets;
import git.aatufutaa.lobby.net.packet.hello.*;
import git.aatufutaa.lobby.net.packet.misc.AlertOutgoingPacket;
import git.aatufutaa.lobby.net.packet.queue.QueueStatusOutgoingPacket;
import git.aatufutaa.server.net.packet.PacketManager;
import git.aatufutaa.server.play.confirm.*;
import git.aatufutaa.server.play.packet.KickOutgoingPacket;

public class LobbyPackets {

    public static void register() {
        PacketManager.registerIncoming(0, HelloIncomingPacket.class);
        PacketManager.registerOutgoing(0, HelloOutgoingPacket.class);

        PacketManager.registerIncoming(1, RequestDataIncomingPacket.class);

        PacketManager.registerOutgoing(2, SendToServerOutgoingPacket.class);
        PacketManager.registerOutgoing(3, KickOutgoingPacket.class);
        PacketManager.registerOutgoing(4, LobbyDataOutgoingPacket.class);

        PacketManager.registerIncoming(5, DataLoadedIncomingPacket.class);

        PacketManager.registerOutgoing(12, QueueStatusOutgoingPacket.class);

        PacketManager.registerOutgoing(14, AlertOutgoingPacket.class);

        PacketManager.registerIncoming(17, ConfirmIncomingPacket.class);
        PacketManager.registerOutgoing(17, ConfirmOutgoingPacket.class);
        PacketManager.registerIncoming(18, ConfirmPingIncomingPacket.class);
        PacketManager.registerOutgoing(18, ConfirmPingOutgoingPacket.class);
        PacketManager.registerOutgoing(19, FlushConfirmOutgoingPacket.class);

        LobbyConfirmPackets.register();
    }

}
