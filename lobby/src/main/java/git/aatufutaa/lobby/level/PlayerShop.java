package git.aatufutaa.lobby.level;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerShop {

    private float commonManaAsGems;
    private float epicManaAsGems;
    private float legendaryManaAsGems;

    private float coinAsGems;

    private float heroTokenAsGems;
}
