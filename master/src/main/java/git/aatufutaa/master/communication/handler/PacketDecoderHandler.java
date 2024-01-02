package git.aatufutaa.master.communication.handler;

import git.aatufutaa.master.communication.packet.IncomingPacket;
import git.aatufutaa.master.communication.packet.PacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
public class PacketDecoderHandler  extends ByteToMessageDecoder {

    @Setter
    private PacketManager packetManager;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        try {

            IncomingPacket<?> packet = this.packetManager.read(buf);
            list.add(packet);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Decoder error for " + ctx.channel().remoteAddress());
            ctx.channel().close();
        }
    }
}
