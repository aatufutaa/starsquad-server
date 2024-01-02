package git.aatufutaa.lobby.level;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PlayerHero {

    private int id;
    private int rarity;
    private List<HeroLevel> levels;

    @Getter
    @AllArgsConstructor
    public static class HeroLevel {
        private int expPrice;
        private int coinPrice;
    }

    public HeroLevel getLevel(int level) {
        return this.levels.size() > level ? this.levels.get(level) : null;
    }
}
