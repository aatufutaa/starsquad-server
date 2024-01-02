package git.aatufutaa.lobby.level;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerLevel {

    private int level;
    private int points;
    private int rewardType;
    private int rewardAmount;
}
