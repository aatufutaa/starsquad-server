package git.aatufutaa.server.play.client;

import git.aatufutaa.server.net.client.ClientBase;
import io.netty.channel.Channel;

public abstract class PlayClient extends ClientBase {


    protected PlayClient(Channel channel) {
        super(channel);
    }
}
