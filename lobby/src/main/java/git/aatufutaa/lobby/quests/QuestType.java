package git.aatufutaa.lobby.quests;

import lombok.Getter;

@Getter
public enum QuestType {
    WIN_GAMES(0, 3, 0, 5);

    private int id;

    private int maxProgress;

    private int rewardType;
    private int rewardAmount;

    private QuestType(int id, int maxProgress, int rewardType, int rewardAmount) {
        this.id = id;
        this.maxProgress = maxProgress;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
    }
}
