package git.aatufutaa.lobby.level;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerHeroPassItem {

    private int tokens;
    private int heroRewardType;
    private int heroRewardAmount;
    private int freeRewardType;
    private int freeRewardAmount;
}
