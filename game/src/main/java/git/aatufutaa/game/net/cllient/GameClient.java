package git.aatufutaa.game.net.cllient;

import git.aatufutaa.game.GameServer;
import git.aatufutaa.server.net.client.ClientBase;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

public class GameClient extends ClientBase {

    @Getter
    @Setter
    private SessionState state = SessionState.HELLO;

    @Getter
    @Setter
    private boolean fallbackToTcp;

    @Getter
    @Setter
    private int nextUdpTick;

    @Getter
    @Setter
    private InetSocketAddress udpAddress;

    @Getter
    @Setter
    private boolean sentStage0;

    public GameClient(Channel channel) {
        super(channel);
    }

    public boolean assertState(SessionState state) {
        if (this.state != state) {
            GameServer.warn(this + " sent wrong state. Sent " + this.state + " ex " + state);
            this.getChannel().close();
            return true;
        }
        return false;
    }

    public boolean canSendUdp() {
        return this.state == SessionState.DONE;
    }
}
