package git.aatufutaa.master.communication.handler;

import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.game.GameServer;
import io.netty.channel.ChannelHandlerContext;

public class GameHandler extends ConnectionHandlerBase<GameServer> {

    private final GameServer server;

    public GameHandler(GameServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IncomingPacket<GameServer> packet) {
        super.channelRead0(ctx, packet);
        packet.handle(this.server);
    }
}
