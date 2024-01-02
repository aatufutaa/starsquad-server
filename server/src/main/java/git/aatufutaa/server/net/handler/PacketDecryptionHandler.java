package git.aatufutaa.server.net.handler;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.net.rc4.RC4;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PacketDecryptionHandler extends ByteToMessageDecoder {

    private final RC4 rc4;
    private byte[] in = new byte[0];

    public PacketDecryptionHandler(RC4 rc4) {
        this.rc4 = rc4;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        try {
            int i = buf.readableBytes(); // NOTE: no need to limit size because length prepender should block too big packet before this

            if (i > this.in.length) {
                this.in = new byte[i];
            }
            buf.readBytes(this.in, 0, i);

            ByteBuf write = ctx.alloc().heapBuffer(i);
            this.rc4.crypt(this.in, 0, write.array(), write.arrayOffset(), i);

            write.writerIndex(i);

            list.add(write);
        } catch (Exception e) {
            e.printStackTrace();
            Server.warn("player decryption error");
            ctx.channel().close();
        }
    }
}
