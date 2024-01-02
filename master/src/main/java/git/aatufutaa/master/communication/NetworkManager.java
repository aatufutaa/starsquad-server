package git.aatufutaa.master.communication;

import git.aatufutaa.master.communication.handler.HandshakeHandler;
import git.aatufutaa.master.communication.handler.PacketDecoderHandler;
import git.aatufutaa.master.communication.handler.PacketEncoderHandler;
import git.aatufutaa.master.communication.packet.handshake.HandshakePacketManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class NetworkManager {

    private static final int NETTY_THREADS = 4;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public void start(int port) throws Exception {
        this.bossGroup = NettyUtil.createEventLoopGroup(NETTY_THREADS);
        this.workerGroup = NettyUtil.createEventLoopGroup(0);

        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
                .channel(NettyUtil.getServerChannel())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(createChannelInitializer())
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture f = b.bind(port).sync();

        if (!f.isSuccess())
            throw new Exception("error");

        System.out.println("Master server listening on " + port);
    }

    public void stop() {
        if (this.bossGroup != null)
            this.bossGroup.shutdownGracefully();
        if (this.workerGroup != null)
            this.workerGroup.shutdownGracefully();
    }

    private static ChannelInitializer<SocketChannel> createChannelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {

                System.out.println("received connection! " + socketChannel.remoteAddress());

                socketChannel.pipeline().addLast(new LengthFieldPrepender(2));
                socketChannel.pipeline().addLast("encoder", new PacketEncoderHandler(HandshakePacketManager.MANAGER));

                socketChannel.pipeline().addLast(new ReadTimeoutHandler(20));

                socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2, 0, 2));
                socketChannel.pipeline().addLast("decoder", new PacketDecoderHandler(HandshakePacketManager.MANAGER));

                socketChannel.pipeline().addLast("handler", new HandshakeHandler());
            }
        };
    }
}
