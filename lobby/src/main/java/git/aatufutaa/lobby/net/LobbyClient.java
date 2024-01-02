package git.aatufutaa.lobby.net;

import git.aatufutaa.server.net.client.ClientBase;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

public class LobbyClient extends ClientBase {

    @Getter
    @Setter
    private boolean sentStage0;

    protected LobbyClient(Channel channel) {
        super(channel);
    }
}
