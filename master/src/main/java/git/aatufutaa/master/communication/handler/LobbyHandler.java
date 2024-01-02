package git.aatufutaa.master.communication.handler;

import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.server.lobby.LobbyServer;
import io.netty.channel.ChannelHandlerContext;

public class LobbyHandler extends ConnectionHandlerBase<LobbyServer> {

    private final LobbyServer server;

    public LobbyHandler(LobbyServer server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IncomingPacket<LobbyServer> packet) {
        super.channelRead0(ctx, packet);
        packet.handle(this.server);
    }

}
