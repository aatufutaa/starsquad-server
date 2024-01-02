package git.aatufutaa.game.net.packet;

import git.aatufutaa.server.net.packet.PacketManager;
import git.aatufutaa.server.play.confirm.*;
import git.aatufutaa.server.play.packet.KickOutgoingPacket;
import git.aatufutaa.game.net.packet.hello.*;

public class GamePackets {

    public static void register() {
        PacketManager.registerIncoming(0, HelloIncomingPacket.class);
        PacketManager.registerOutgoing(0, HelloOutgoingPacket.class);

        PacketManager.registerIncoming(1, RequestDataIncomingPacket.class);
        PacketManager.registerOutgoing(2, GameDataOutgoingPacket.class);
        PacketManager.registerOutgoing(3, KickOutgoingPacket.class);
        PacketManager.registerOutgoing(4, StartUdpOutgoingPacket.class);
        PacketManager.registerIncoming(5, UdpReadyIncomingPacket.class);
        PacketManager.registerOutgoing(6, DynamicGameDataOutgoingPacket.class);
        PacketManager.registerIncoming(7, DataLoadedIncomingPacket.class);

        PacketManager.registerIncoming(8, TickIncomingPacket.class);
        PacketManager.registerOutgoing(8, TickOutgoingPacket.class);

        PacketManager.registerIncoming(9, HomeIncomingPacket.class);
        PacketManager.registerOutgoing(9, GameResultOutgoingPacket.class);

        PacketManager.registerOutgoing(10, SendToServerOutgoingPacket.class);

        PacketManager.registerIncoming(17, ConfirmIncomingPacket.class);
        PacketManager.registerOutgoing(17, ConfirmOutgoingPacket.class);
        PacketManager.registerIncoming(18, ConfirmPingIncomingPacket.class);
        PacketManager.registerOutgoing(18, ConfirmPingOutgoingPacket.class);
        PacketManager.registerOutgoing(19, FlushConfirmOutgoingPacket.class);

        PacketManager.registerIncoming(20, ExitIncomingPacket.class);
        PacketManager.registerOutgoing(20, GameDataSavedOutgoingPacket.class);
    }
}
