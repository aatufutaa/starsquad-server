package git.aatufutaa.server.communication;

import git.aatufutaa.server.communication.packet.MasterIncomingPacket;

public interface MasterListener {

    void onConnected();
    void onDisconnected();
    void onRead(MasterIncomingPacket packet);

}
