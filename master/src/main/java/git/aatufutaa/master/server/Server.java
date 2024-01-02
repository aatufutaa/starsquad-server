package git.aatufutaa.master.server;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.OutgoingPacket;
import git.aatufutaa.master.communication.packet.handshake.PingOutgoingPacket;
import io.netty.channel.Channel;
import lombok.Getter;

public abstract class Server {

    @Getter
    private final int serverId;

    private final Channel channel;

    @Getter
    private final ServerLocation serverLocation;

    public Server(int serverId, ServerLocation location, Channel channel) {
        this.serverId = serverId;
        this.serverLocation = location;
        this.channel = channel;
    }

    public void disconnect() {
        this.channel.close();
    }

    public boolean isConnected() {
        return this.channel.isOpen();
    }

    @Override
    public String toString() {
        return "serverId=" + this.serverId + ", serverLocation=" + this.serverLocation.name();
    }


    public void sendPacket(OutgoingPacket packet) {
        // ensure all packets get sent in order
        if (!MasterServer.getInstance().getServerManager(this.getServerLocation()).isInServerThread()) {

            if (this.channel.eventLoop().inEventLoop()) {
                this.channel.writeAndFlush(packet);
                return;
            }

            MasterServer.getInstance().getServerManager(this.getServerLocation()).runOnServerThread(() -> this.sendPacket(packet));
            return;
        }

        System.out.println("write packet " + packet);
        this.channel.eventLoop().execute(() -> this.channel.writeAndFlush(packet));
    }

    public void tick() {
        this.channel.writeAndFlush(new PingOutgoingPacket());
    }

    public void crash() {
    }

    public abstract ServerType getServerType();
}
