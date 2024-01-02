package git.aatufutaa.lobby.net;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.channel.ChannelHandlerContext;

public class LobbyConnectionHandler extends ConnectionHandlerBase {

    private LobbyClient client;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.client = new LobbyClient(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IncomingPacket packet) throws Exception {
        System.out.println("read player packet " + packet);
        packet.handle(this.client);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel inactive " + ctx.channel().remoteAddress());

        LobbyServer.getInstance().runOnMainThread(() -> {
            if (this.client.isPlayerIdSet()) {
                Session session = LobbyServer.getInstance().getSessionManager().getSession(this.client.getPlayerId());
                if (session != null)
                    session.onDisconnect(this.client);
            }
        });
    }
}
