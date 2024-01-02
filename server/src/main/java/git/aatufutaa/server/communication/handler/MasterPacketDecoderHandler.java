package git.aatufutaa.server.communication.handler;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.communication.packet.MasterIncomingPacket;
import git.aatufutaa.server.communication.packet.MasterPacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MasterPacketDecoderHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        try {

            MasterIncomingPacket packet = MasterPacketManager.read(buf);
            list.add(packet);

        } catch (Exception e) {
            e.printStackTrace();
            Server.warn("master decoder error");
            ctx.channel().close();
        }
    }
}
