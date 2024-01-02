package git.aatufutaa.master.server.login.packet;

import git.aatufutaa.master.communication.packet.PingPacketManager;

public class LoginPacketManager extends PingPacketManager {

    public static final LoginPacketManager MANAGER = new LoginPacketManager();

    public LoginPacketManager() {
        this.registerIncoming(2, LoginIncomingPacket.class);
        this.registerOutgoing(2, LoginOutgoingPacket.class);
    }
}
