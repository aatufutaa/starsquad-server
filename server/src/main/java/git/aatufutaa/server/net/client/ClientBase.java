package git.aatufutaa.server.net.client;

import git.aatufutaa.server.net.handler.PacketDecryptionHandler;
import git.aatufutaa.server.net.handler.PacketEncryptionHandler;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import git.aatufutaa.server.net.rc4.RC4;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoop;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Getter
public class ClientBase {

    private final Channel channel;

    @Setter
    private int playerId;
    private boolean playerIdSet;

    @Setter
    private boolean dataLoaded;

    protected ClientBase(Channel channel) {
        this.channel = channel;
    }

    public void confirmSession(int playerId) {
        this.playerId = playerId;
        this.playerIdSet = true;
    }

    public void sendPacket(OutgoingPacket packet, Consumer<ChannelFuture> callback) {
        EventLoop eventLoop = this.channel.eventLoop();
        if (eventLoop.inEventLoop()) {
            callback.accept(this.channel.writeAndFlush(packet));
        } else {
            eventLoop.execute(() -> callback.accept(this.channel.writeAndFlush(packet)));
        }
    }

    public void sendPacketAndClose(OutgoingPacket packet) {
        this.sendPacket(packet, callback -> callback.addListener(f -> this.channel.close()));
    }

    public void sendPacket(OutgoingPacket packet) {
        this.sendPacket(packet, f -> {
        });
    }

    public void sendPacketSafe(OutgoingPacket packet) {
        if (!this.dataLoaded) {
            System.out.println("cant send " + packet);
            return;
        }
        this.sendPacket(packet);
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isActive();
    }

    public void enableEncryption(byte[] key) {
        try {
            this.channel.pipeline().addBefore("encoder", "encrypter", new PacketEncryptionHandler(new RC4(key)));
            this.channel.pipeline().addBefore("decoder", "decrypter", new PacketDecryptionHandler(new RC4(key)));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAILED TO ENABLE ENCRYPTION for " + this.channel.remoteAddress());
            this.channel.close();
        }
    }

    @Override
    public String toString() {
        return "client id=" + this.playerId + " add=" + this.channel.remoteAddress();
    }
}
