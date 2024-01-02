package git.aatufutaa.game.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameType {
    TUTORIAL(1, 1),
    LAST_HERO_STANDING(8, 1)
    ;


    private final int teamCount;
    private final int teamSize;
}
