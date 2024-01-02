package git.aatufutaa.game.session;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.game.Game;
import git.aatufutaa.game.master.packet.HomeMasterOutgoingPacket;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.server.play.session.SessionBase;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Session extends SessionBase {

    @Setter
    private byte[] key;

    private final String name;

    // udp packet id
    @Setter
    private boolean hasUdpId;
    @Setter
    private short udpId;

    @Setter
    private Game game;

    @Setter
    private boolean sentHome;

    @Getter
    @Setter
    private int waitingLatestAcceptedId;
    @Getter
    @Setter
    private boolean sendConfirmPackets;

    @Getter
    private int disconnectTicks;
    @Getter
    @Setter
    private boolean allowReconnect = true;

    public Session(int playerId, String secret, String name) {
        super(playerId, secret);
        this.name = name;
    }

    public void onConnect(GameClient client) {
        this.client = client;
    }

    public void onDisconnect(GameClient client) {
        if (this.client == client) { // if channel didnt close because new client connected
            this.client.getChannel().close(); // should be already closed
            this.client = null;
            // TODO: tell game to bot
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.client == null) {
            ++this.disconnectTicks;
            if (this.disconnectTicks > 20 * 10) {
                if (this.allowReconnect) {
                    this.getPacketConfirmManager().reset();
                    this.allowReconnect = false;
                }

                if (this.game == null && this.disconnectTicks == 30) {
                    GameServer.getInstance().getMasterConnection().sendPacket(new HomeMasterOutgoingPacket(this.playerId));
                }
            }
        } else {
            this.disconnectTicks = 0;
        }
    }
}
