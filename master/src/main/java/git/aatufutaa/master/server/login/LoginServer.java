package git.aatufutaa.master.server.login;

import git.aatufutaa.master.server.Server;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.ServerType;
import io.netty.channel.Channel;

public class LoginServer extends Server {

    public LoginServer(int serverId, ServerLocation location, Channel channel) {
        super(serverId, location, channel);
    }

    @Override
    public ServerType getServerType() {
        return ServerType.LOGIN;
    }

    @Override
    public String toString() {
        return "LoginServer{" + super.toString() + "}";
    }
}
