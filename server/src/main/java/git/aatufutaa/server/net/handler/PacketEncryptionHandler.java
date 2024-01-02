package git.aatufutaa.server.net.handler;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.net.rc4.RC4;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncryptionHandler extends MessageToByteEncoder<ByteBuf> {

    private final RC4 rc4;

    private byte[] out = new byte[0];

    public PacketEncryptionHandler(RC4 rc4) {
        this.rc4 = rc4;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf buf, ByteBuf byteBuf) throws Exception {
        try {
            int i = buf.readableBytes();

            byte[] in = new byte[i];
            buf.readBytes(in);

            if (this.out.length < i) {
                this.out = new byte[i];
            }

            this.rc4.crypt(in, 0, this.out, 0, i);

            byteBuf.writeBytes(this.out, 0, i);
        } catch (Exception e) {
            e.printStackTrace();
            Server.warn("player encryption error");
            ctx.channel().close();
        }
    }
}
