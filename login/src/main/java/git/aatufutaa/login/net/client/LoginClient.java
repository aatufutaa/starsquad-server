package git.aatufutaa.login.net.client;

import git.aatufutaa.server.net.client.ClientBase;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;

public class LoginClient extends ClientBase {

    @Getter
    @Setter
    private ProtocolState protocolState;

    @Setter
    @Getter
    private int version;
    @Setter
    @Getter
    private boolean android;
    @Setter
    @Getter
    private byte[] key;

    public LoginClient(Channel channel) {
        super(channel);
        this.protocolState = ProtocolState.HELLO;
    }

    public void assertProtocolState(ProtocolState state) throws Exception {
        if (this.protocolState != state) {
            throw new Exception("wrong protocol state. expected " + state + " was " + this.protocolState);
        }
    }
}
