package git.aatufutaa.server.play.session;

import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import git.aatufutaa.server.play.confirm.ConfirmPingOutgoingPacket;
import git.aatufutaa.server.play.confirm.PacketConfirmManager;
import git.aatufutaa.server.play.packet.KickOutgoingPacket;
import lombok.Getter;
import lombok.Setter;

public class SessionBase {

    @Getter
    protected final int playerId;
    @Getter
    private final String secret;

    @Getter
    private final PacketConfirmManager packetConfirmManager = new PacketConfirmManager();

    @Getter
    @Setter
    protected ClientBase client;

    public SessionBase(int playerId, String secret) {
        this.playerId = playerId;
        this.secret = secret;
    }

    public void kick(String msg) {
        if (this.client != null) {
            if (msg != null) {
                this.client.sendPacketAndClose(new KickOutgoingPacket(msg));
            } else {
                this.client.getChannel().close();
            }
            this.client = null;
        }
    }

    public void onDisconnectedTooLong(){
        this.packetConfirmManager.reset();
    }

    public void sendPacket(OutgoingPacket packet) {
        if (this.client != null) this.client.sendPacket(packet);
    }

    public void sendPacketAndClose(OutgoingPacket packet) {
        if (this.client != null) {
            this.client.sendPacketAndClose(packet);
            this.client = null;
        }
    }

    public void sendConfirmPacket(OutgoingPacket packet) {
        this.packetConfirmManager.send(this, packet);
    }

    public void sendPacketSafe(OutgoingPacket packet) {
        if (this.client != null) this.client.sendPacketSafe(packet);
    }

    @Override
    public String toString() {
        return "session id=" + this.playerId;
    }

    public void tick() {
        if (this.client != null && this.client.isDataLoaded()) {
            this.client.sendPacket(new ConfirmPingOutgoingPacket(this.packetConfirmManager.getLatestAcceptedId()));
        }
    }
}
