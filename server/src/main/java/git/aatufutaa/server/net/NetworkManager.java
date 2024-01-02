package git.aatufutaa.server.net;

import git.aatufutaa.server.communication.NettyUtil;
import git.aatufutaa.server.net.handler.ConnectionHandlerBase;
import git.aatufutaa.server.net.handler.PacketDecoderHandler;
import git.aatufutaa.server.net.handler.PacketEncoderHandler;
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

    private final Class<? extends ConnectionHandlerBase> clazz;

    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public NetworkManager(Class<? extends ConnectionHandlerBase> clazz) {
        this.clazz = clazz;
        this.bossGroup = NettyUtil.createEventLoopGroup(1);
        this.workerGroup = NettyUtil.createEventLoopGroup(0);
    }

    public void start(String host, int port) throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(this.bossGroup, this.workerGroup)
                .channel(NettyUtil.getServerChannel())
                .childHandler(this.createChannelInitializer())
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        ChannelFuture f = b.bind("0.0.0.0", port).sync();

        if (!f.isSuccess()) {
            throw new Exception("cant start server!!!");
        }

        System.out.println("Server listening on " + f.channel().localAddress());
    }

    private ChannelInitializer<SocketChannel> createChannelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                System.out.println("Client connected " + socketChannel.remoteAddress());

                socketChannel.pipeline().addLast("prepender", new LengthFieldPrepender(2));
                socketChannel.pipeline().addLast("encoder", new PacketEncoderHandler());

                socketChannel.pipeline().addLast(new ReadTimeoutHandler(20));

                socketChannel.pipeline().addLast("splitter", new LengthFieldBasedFrameDecoder(1024 * 4, 0, 2, 0, 2));
                socketChannel.pipeline().addLast("decoder", new PacketDecoderHandler());

                socketChannel.pipeline().addLast(NetworkManager.this.clazz.getConstructor().newInstance());
            }
        };
    }

    public void stop() {
        this.workerGroup.shutdownGracefully();
        this.bossGroup.shutdownGracefully();
    }
}
