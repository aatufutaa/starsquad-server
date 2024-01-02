package git.aatufutaa.master.party;

import git.aatufutaa.master.session.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PartyManager {

    private int currentId;
    private final Map<Integer, Party> parties = new HashMap<>();

    private ExecutorService thread;

    public PartyManager() {
    }

    public void runOnPartyThread(Runnable r) {
        this.thread.execute(r);
    }

    public void start() {
        this.thread = Executors.newSingleThreadExecutor();
    }

    public void stop() {
        if (this.thread != null)
            this.thread.shutdown();
    }

    public void getParty(int partyId, Consumer<Party> callback) {
        this.runOnPartyThread(() -> {
            Party party = this.parties.get(partyId);
            callback.accept(party);
        });
    }

    public void createParty(Session session, Consumer<Party> callback) {
        this.runOnPartyThread(() -> {
            Party party = new Party(this.currentId++, session);
            this.parties.put(party.getPartyId(), party);
            callback.accept(party);
        });
    }

    public void destroyParty(Party party) {
        this.runOnPartyThread(() -> this.parties.remove(party.getPartyId()));
    }
}
