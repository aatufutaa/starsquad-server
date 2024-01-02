package git.aatufutaa.lobby.quests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class QuestData {

    private int id;
    @Setter private int amount;
    @Setter private int claimIndex;

}
