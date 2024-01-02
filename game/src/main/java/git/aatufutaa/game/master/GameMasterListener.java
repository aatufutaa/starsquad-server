package git.aatufutaa.game.master;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.server.communication.MasterListener;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;

public class GameMasterListener implements MasterListener {

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {
        GameServer.getInstance().crash("Lost connection to master");
    }

    @Override
    public void onRead(MasterIncomingPacket packet) {
        packet.handle();
    }
}
