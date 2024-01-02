package git.aatufutaa.master.queue;

import git.aatufutaa.master.session.Session;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Consumer;

@Getter
public class QueueData {

    private boolean left;

    private final int rating;

    private final Session session;

    @Setter
    private List<Session> members;

    public QueueData(int rating, Session session) {
        this.rating = rating;
        this.session = session;
    }

    public int getPlayerCount() {
        int playerCount = 1;
        if (this.members != null) playerCount += this.members.size();
        return playerCount;
    }

    public void leave(Session session) {
        if (this.session != session) return;
        this.left = true;
    }

    public void forEach(Consumer<Session> callback) {
        callback.accept(this.session);

        if (this.members != null) {
            for (Session member : this.members) {
                callback.accept(member);
            }
        }
    }

    public boolean isInDefaultRatingRange(int rating) {
        int min = this.rating - 100;
        int max = this.rating + 100;
        return rating >= min && rating <= max;
    }
}

