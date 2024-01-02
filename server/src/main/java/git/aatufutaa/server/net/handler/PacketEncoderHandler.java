package git.aatufutaa.server.net.handler;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import git.aatufutaa.server.net.packet.PacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoderHandler extends MessageToByteEncoder<OutgoingPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, OutgoingPacket packet, ByteBuf buf) throws Exception {
        try {
            PacketManager.write(packet, buf);
        } catch (Exception e) {
            e.printStackTrace();
            Server.warn("player encoder error for packet " + packet);
            ctx.channel().close();
        }
    }
}
