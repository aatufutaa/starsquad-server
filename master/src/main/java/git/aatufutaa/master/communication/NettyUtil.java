package git.aatufutaa.master.communication;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyUtil {
    private static final boolean EPOLL = Epoll.isAvailable();

    public static EventLoopGroup createEventLoopGroup(int threads) {
        return EPOLL ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
    }

    public static Class<? extends ServerChannel> getServerChannel() {
        return EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }
}
