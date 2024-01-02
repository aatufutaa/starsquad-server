package git.aatufutaa.master.command;

import git.aatufutaa.master.MasterServer;

public class StatsCommand implements Command {
    @Override
    public void handle(String[] args) {
        MasterServer.log("stats -> 123");
    }
}
