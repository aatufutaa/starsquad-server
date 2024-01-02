package git.aatufutaa.login.net;

import git.aatufutaa.login.net.packet.*;
import git.aatufutaa.server.net.packet.PacketManager;

public class LoginPackets {

    public static void register() {
        PacketManager.registerIncoming(0, HelloIncomingPacket.class);
        PacketManager.registerOutgoing(0, HelloOutgoingPacket.class);

        PacketManager.registerIncoming(1, RequestAccessKeyIncomingPacket.class);
        PacketManager.registerOutgoing(1, RequestAccessKeyOutgoingPacket.class);

        PacketManager.registerIncoming(2, EncryptionIncomingPacket.class);
        PacketManager.registerOutgoing(2, EncryptionOutgoingPacket.class);

        PacketManager.registerIncoming(3, LoginIncomingPacket.class);
        PacketManager.registerOutgoing(3, LoginOutgoingPacket.class);
    }
}
