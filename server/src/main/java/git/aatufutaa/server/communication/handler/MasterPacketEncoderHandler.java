package git.aatufutaa.server.communication.handler;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import git.aatufutaa.server.communication.packet.MasterPacketManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MasterPacketEncoderHandler extends MessageToByteEncoder<MasterOutgoingPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MasterOutgoingPacket packet, ByteBuf buf) {
        try {

            MasterPacketManager.write(packet, buf);

        } catch (Exception e) {
            e.printStackTrace();
            Server.warn("master encoder error");
            ctx.channel().close();
        }
    }
}
