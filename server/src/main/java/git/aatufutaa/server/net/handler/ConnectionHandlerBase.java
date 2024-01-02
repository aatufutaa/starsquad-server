package git.aatufutaa.server.net.handler;

import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

public abstract class ConnectionHandlerBase extends SimpleChannelInboundHandler<IncomingPacket> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel active " + ctx.channel().remoteAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        System.out.println("NETTY HANDLER ERROR!!!!");
        System.out.println("NETTY HANDLER ERROR!!!!");
        System.out.println("NETTY HANDLER ERROR!!!!");
        System.out.println("NETTY HANDLER ERROR!!!!");
        System.out.println("NETTY HANDLER ERROR!!!!");
        System.out.println("NETTY HANDLER ERROR!!!!");
        System.out.println("NETTY HANDLER ERROR!!!!");
        System.out.println("NETTY HANDLER ERROR!!!!");

        if (cause instanceof ReadTimeoutException) {
            System.out.println(ctx.channel().remoteAddress() + " timeout");
        } else {
            System.out.println(ctx.channel().remoteAddress() + " error");
            cause.printStackTrace();
        }

        ctx.channel().close();

        //super.exceptionCaught(ctx, cause);
    }
}
