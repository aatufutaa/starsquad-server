package git.aatufutaa.login.net;

import git.aatufutaa.login.net.client.LoginClient;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.channel.ChannelHandlerContext;

public class LoginConnectionHandler extends ConnectionHandlerBase {

    private LoginClient client;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.client = new LoginClient(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IncomingPacket packet) throws Exception {
        System.out.println("read player packet " + packet);
        packet.handle(this.client);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel inactive " + ctx.channel().remoteAddress());
    }
}
