package git.aatufutaa.master.communication.handler;

import git.aatufutaa.master.communication.packet.OutgoingPacket;
import git.aatufutaa.master.communication.packet.PacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
public class PacketEncoderHandler extends MessageToByteEncoder<OutgoingPacket> {

    @Setter
    private PacketManager packetManager;

    @Override
    protected void encode(ChannelHandlerContext ctx, OutgoingPacket packet, ByteBuf buf) {
        try {

            this.packetManager.write(packet, buf);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Encoder error for " + ctx.channel().remoteAddress());
            ctx.channel().close();
        }
    }
}
