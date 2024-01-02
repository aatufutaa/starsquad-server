package git.aatufutaa.master.communication.handler;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.communication.packet.PacketManager;
import git.aatufutaa.master.communication.packet.handshake.HelloIncomingPacket;
import git.aatufutaa.master.communication.packet.handshake.HelloOutgoingPacket;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.Server;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.game.GamePacketManager;
import git.aatufutaa.master.server.game.GameServer;
import git.aatufutaa.master.server.lobby.packet.LobbyPacketManager;
import git.aatufutaa.master.server.lobby.LobbyServer;
import git.aatufutaa.master.server.login.packet.LoginPacketManager;
import git.aatufutaa.master.server.login.LoginServer;
import git.aatufutaa.master.server.ServerType;
import io.netty.channel.ChannelHandlerContext;

public class HandshakeHandler extends ConnectionHandlerBase<Server> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IncomingPacket<Server> packet) {
        System.out.println("reading " + ctx.channel().remoteAddress() + " " + packet);

        if (packet instanceof HelloIncomingPacket) {

            ServerType serverType = ((HelloIncomingPacket)packet).getServerType();
            ServerLocation location = ((HelloIncomingPacket) packet).getLocation();

            MasterServer.getInstance().getServerManager(location).runOnServerThread(() -> {

                Server server = MasterServer.getInstance().getServerManager(location).addServer(serverType, location, ctx.channel());

                PacketManager packetManager;
                ConnectionHandlerBase<?> handler;

                switch (serverType) {
                    case LOGIN:
                        packetManager = LoginPacketManager.MANAGER;
                        handler = new LoginHandler((LoginServer) server);
                        break;
                    case LOBBY:
                        packetManager = LobbyPacketManager.MANAGER;
                        handler = new LobbyHandler((LobbyServer) server);
                        break;
                    case GAME:
                        packetManager = GamePacketManager.MANAGER;
                        handler = new GameHandler((GameServer) server);
                        break;
                    default:
                        return;
                }

                if (serverType != ServerType.LOGIN) {
                    PlayServer playServer = (PlayServer) server;
                    playServer.setHost(((HelloIncomingPacket) packet).getHost());
                    playServer.setPort(((HelloIncomingPacket) packet).getPort());
                }

                ctx.channel().writeAndFlush(new HelloOutgoingPacket()).addListener(f -> {
                    ctx.channel().pipeline().replace("handler", "handler", handler);

                    PacketEncoderHandler encoder = (PacketEncoderHandler) ctx.channel().pipeline().get("encoder");
                    PacketDecoderHandler decoder = (PacketDecoderHandler) ctx.channel().pipeline().get("decoder");
                    encoder.setPacketManager(packetManager);
                    decoder.setPacketManager(packetManager);

                    System.out.println("Handshake done! server " + serverType);
                });
            });
        }
    }
}
