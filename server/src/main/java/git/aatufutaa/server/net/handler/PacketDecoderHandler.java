package git.aatufutaa.server.net.handler;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.net.packet.IncomingPacket;
import git.aatufutaa.server.net.packet.PacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoderHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        try {

            IncomingPacket packet = PacketManager.read(buf);
            list.add(packet);

        } catch (Exception e) {
            e.printStackTrace();
            Server.warn("player decoder error");
            ctx.channel().close();
        }
    }
}
