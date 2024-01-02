package git.aatufutaa.lobby.master;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.server.communication.MasterListener;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;

public class LobbyMasterListener implements MasterListener {

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
        LobbyServer.getInstance().crash("Lost connection to master");
    }

    @Override
    public void onRead(MasterIncomingPacket packet) {
        packet.handle();
    }
}
