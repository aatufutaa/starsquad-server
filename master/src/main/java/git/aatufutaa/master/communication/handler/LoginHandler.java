package git.aatufutaa.master.communication.handler;

import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.login.LoginServer;
import io.netty.channel.ChannelHandlerContext;

public class LoginHandler extends ConnectionHandlerBase<LoginServer> {

    private final LoginServer server;

    public LoginHandler(LoginServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IncomingPacket<LoginServer> packet) {
        super.channelRead0(ctx, packet);
        packet.handle(this.server);
    }
}
