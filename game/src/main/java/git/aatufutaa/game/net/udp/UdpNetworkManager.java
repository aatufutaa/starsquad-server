package git.aatufutaa.game.net.udp;

import git.aatufutaa.server.communication.NettyUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import lombok.Getter;

import java.net.InetSocketAddress;

public class UdpNetworkManager {

    private static final int PACKET_LIMIT = 1024;

    private final EventLoopGroup bossGroup = NettyUtil.createEventLoopGroup(0);

    private Channel channel;
    @Getter private int port;

    public UdpNetworkManager() {

    }

    public void start() throws InterruptedException {
        Bootstrap b = new Bootstrap()
                .group(this.bossGroup)
                .channel(NettyUtil.getDatagramChannel())
                //.option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(PACKET_LIMIT))
                .handler(new UdpPacketHandler());

        ChannelFuture future = b.bind("0.0.0.0", 0).sync();

        Channel channel = future.channel();
        InetSocketAddress address = (InetSocketAddress) channel.localAddress();

        this.channel = channel;
        this.port = address.getPort();

        System.out.println("UDP server bound to " + address);
    }

    public void stop() {
        this.bossGroup.shutdownGracefully();
    }

    public void sendPacket(OutgoingPacket packet, InetSocketAddress receiver) {
        Runnable r = () -> {
            ByteBuf buf = Unpooled.buffer();

            packet.write(buf);

            DatagramPacket msg = new DatagramPacket(buf, receiver);

            this.channel.writeAndFlush(msg);
        };

        if (this.channel.eventLoop().inEventLoop()) {
            r.run();
        } else {
            this.channel.eventLoop().execute(r);
        }
    }
}
