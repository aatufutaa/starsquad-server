package git.aatufutaa.game.net;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.game.net.cllient.GameClient;
import git.aatufutaa.game.session.Session;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.channel.ChannelHandlerContext;

public class GameConnectionHandler extends ConnectionHandlerBase {

    private GameClient client;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.client = new GameClient(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, IncomingPacket packet) throws Exception {
        System.out.println("read player packet " + packet);
        packet.handle(this.client);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel inactive " + ctx.channel().remoteAddress());

        GameServer.getInstance().runOnMainThread(() -> {
            if (this.client.isPlayerIdSet()) {
                Session session = GameServer.getInstance().getSessionManager().getSession(this.client.getPlayerId());
                if (session != null)
                    session.onDisconnect(this.client);
            }
        });
    }
}
