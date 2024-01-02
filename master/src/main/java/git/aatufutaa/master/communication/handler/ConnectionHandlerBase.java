package git.aatufutaa.master.communication.handler;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.communication.packet.handshake.PingIncomingPacket;
import git.aatufutaa.master.server.Server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import java.net.SocketException;

public class ConnectionHandlerBase<T extends Server> extends SimpleChannelInboundHandler<IncomingPacket<T>> {

    public ConnectionHandlerBase() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channel active " + ctx.channel().remoteAddress());

        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IncomingPacket<T> packet) {
        if (packet.getClass() != PingIncomingPacket.class)
            MasterServer.log("read " + ctx.channel().remoteAddress() + " " + packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println(ctx.channel().read() + " inactive");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();

        if (cause instanceof ReadTimeoutException) {
            MasterServer.warn(ctx.channel().remoteAddress() + " timeout");
        } else if (cause instanceof SocketException) {
            MasterServer.warn(ctx.channel().remoteAddress() + " " + cause.getCause() + " " + cause.getMessage());
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
