package git.aatufutaa.server.communication;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.communication.handler.MasterConnectionHandler;
import git.aatufutaa.server.communication.handler.MasterPacketDecoderHandler;
import git.aatufutaa.server.communication.handler.MasterPacketEncoderHandler;
import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import git.aatufutaa.server.communication.packet.packets.WaitForResponseIncomingPacket;
import git.aatufutaa.server.communication.packet.packets.WaitForResponseMasterOutgoingPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MasterConnection {

    private EventLoopGroup bossGroup;

    private Channel channel;

    private ScheduledExecutorService requestThread;
    private final Map<Integer, Request> requests = new HashMap<>();
    private int currentRequestId;

    public MasterConnection() {
    }

    public void start() {
        this.bossGroup = NettyUtil.createEventLoopGroup(1);
        this.requestThread = Executors.newSingleThreadScheduledExecutor();
        this.requestThread.scheduleAtFixedRate(this::tickRequests, 1L, 1L, TimeUnit.SECONDS);
    }

    public void connect(String ip, int port, MasterListener listener) throws Exception {
        Server.log("Connecting to master on " + ip + ":" + port);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.bossGroup)
                .channel(NettyUtil.getSocketChannel())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        channel.pipeline().addLast(new LengthFieldPrepender(2));
                        channel.pipeline().addLast(new MasterPacketEncoderHandler());

                        channel.pipeline().addLast(new ReadTimeoutHandler(20));

                        channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Short.MAX_VALUE, 0, 2, 0, 2));
                        channel.pipeline().addLast(new MasterPacketDecoderHandler());

                        channel.pipeline().addLast(new MasterConnectionHandler(MasterConnection.this, listener));
                    }
                });

        ChannelFuture future = bootstrap.connect(ip, port).sync();

        if (!future.isSuccess()) {
            throw new Exception("error");
        }

        this.channel = future.channel();

        Server.log("Connected to master server");
        ;
    }

    public void sendPacket(MasterOutgoingPacket packet) {
        Runnable r = () -> this.channel.writeAndFlush(packet);
        if (this.channel.eventLoop().inEventLoop()) {
            r.run();
        } else {
            this.channel.eventLoop().execute(r);
        }
    }

    public void stop() {
        this.disconnect();
        this.bossGroup.shutdownGracefully();
    }

    public boolean isConnected() {
        return this.channel != null && this.channel.isOpen();
    }

    public void disconnect() {
        if (this.channel != null) {
            this.channel.close();
            this.channel = null;
        }
    }

    public void sendPacketWithResponse(WaitForResponseMasterOutgoingPacket packet, RequestResponse callback) {
        this.requestThread.execute(() -> {
            int requestId = ++this.currentRequestId;
            Request request = new Request();
            request.callback = callback;
            this.requests.put(requestId, request);

            packet.setResponseId(requestId);
            this.sendPacket(packet);
        });
    }

    public interface RequestResponse {
        void onAccept(WaitForResponseIncomingPacket packet);
        void onTimeout();
    }

    public static class Request {
        private RequestResponse callback;
        private int ticks;
    }

    public void handleRequest(int id, WaitForResponseIncomingPacket packet) {
        this.requestThread.execute(() -> {
            Request request = this.requests.get(id);
            if (request != null) {
                request.callback.onAccept(packet);
                this.requests.remove(id);
            }
        });
    }

    private void tickRequests() {
        Iterator<Map.Entry<Integer, Request>> iterator = this.requests.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Integer, Request> entry = iterator.next();

            Request request = entry.getValue();
            request.ticks++;

            if (request.ticks > 5) {
                request.callback.onTimeout();
                iterator.remove();
            }
        }
    }
}
