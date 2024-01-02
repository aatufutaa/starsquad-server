package git.aatufutaa.server.communication.handler;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.communication.MasterConnection;
import git.aatufutaa.server.communication.MasterListener;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import git.aatufutaa.server.communication.packet.packets.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MasterConnectionHandler extends SimpleChannelInboundHandler<MasterIncomingPacket> {

    private MasterConnection masterConnection;
    private final MasterListener listener;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Server.log("master channel active");

        ctx.channel().writeAndFlush(new HelloMasterOutgoingPacket(Server.getServer().getServerType(),
                Server.getServer().getLocation(),
                Server.getServer().getHost(),
                Server.getServer().getPort()));

        ctx.fireChannelActive();

        this.listener.onConnected();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MasterIncomingPacket packet) throws Exception {
        if (!(packet instanceof PingMasterIncomingPacket))
            Server.log("master read " + packet);

        if (packet instanceof HelloMasterIncomingPacket) {
            System.out.println("master hello");
        } else if (packet instanceof PingMasterIncomingPacket) {
            ctx.channel().writeAndFlush(new PingMasterOutgoingPacket());
        } else if (packet instanceof WaitForResponseIncomingPacket) {
            ((WaitForResponseIncomingPacket) packet).handle(this.masterConnection);
        } else {
            this.listener.onRead(packet);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Server.log("connection lost to master");

        this.listener.onDisconnected();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();

        if (cause instanceof ReadTimeoutException) {
            Server.warn(ctx.channel().remoteAddress() + " timeout");
        }

        super.exceptionCaught(ctx, cause);
    }
}
