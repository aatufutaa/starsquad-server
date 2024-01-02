package git.aatufutaa.master.command;

import git.aatufutaa.master.MasterServer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {

    private boolean running;

    private Map<String, Command> commands = new HashMap<>();

    public CommandManager() {

        this.commands.put("stats", new StatsCommand());
        this.commands.put("servers", new ServersCommand());

    }

    public void start() throws IOException {
        this.running = true;

        Terminal terminal = TerminalBuilder.builder().build();
        DefaultParser parser = new DefaultParser();
        LineReader reader = LineReaderBuilder.builder().terminal(terminal).parser(parser).build();
        while(this.running) {
            try {
                String line = reader.readLine("master> ");

                String[] raw = line.split(" ");
                String cmd = raw[0].toLowerCase();

                Command command = this.commands.get(cmd);
                if (command == null) {
                    MasterServer.warn("cant find command " + cmd);
                    continue;
                }

                String[] args = new String[raw.length - 1];
                System.arraycopy(raw, 1, args, 0, raw.length - 1);
                try {
                    command.handle(args);
                } catch ( Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void stop() {
        this.running = false;
    }
}
