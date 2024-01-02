package git.aatufutaa.master.command;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.server.ServerLocation;

public class ServersCommand implements Command {
    @Override
    public void handle(String[] args) {
        for (ServerLocation location : ServerLocation.values()) {
            MasterServer.getInstance().getServerManager(location).runOnServerThread(() -> {
                MasterServer.getInstance().getServerManager(location).logServers();
            });
        }
    }
}
