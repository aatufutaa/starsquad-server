package git.aatufutaa.master.queue;

import lombok.Getter;

@Getter
public enum QueueType {

    //TOWER_WARS(3, 2),
    //CANDY_RUSH(3, 2),
    LAST_HERO_STANDING(1, 8);

    private final int teamSize;
    private final int teamCount;
    private final int maxPlayers;

    private QueueType(int teamSize, int teamCount) {
        this.teamSize = teamSize;
        this.teamCount = teamCount;
        this.maxPlayers = teamSize * teamCount;
    }
}
