package git.aatufutaa.server.communication;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyUtil {

    private static final boolean EPOLL = Epoll.isAvailable();

    public static EventLoopGroup createEventLoopGroup(int threads) {
        return EPOLL ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
    }

    public static Class<? extends ServerChannel> getServerChannel() {
        return EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public static Class<? extends DatagramChannel> getDatagramChannel() {
        return EPOLL ? EpollDatagramChannel.class : NioDatagramChannel.class;
    }

    public static Class<? extends SocketChannel> getSocketChannel() {
        return EPOLL ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
