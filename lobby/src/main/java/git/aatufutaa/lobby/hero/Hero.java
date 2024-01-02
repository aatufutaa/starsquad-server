package git.aatufutaa.lobby.hero;

import git.aatufutaa.lobby.level.PlayerHero;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Hero {

    private PlayerHero serverHero;
    @Setter private int level;
    private int rating;

    public void write(ByteBuf buf) {
        buf.writeByte(this.serverHero.getId());
        buf.writeByte(this.level);
        buf.writeShortLE(this.rating);
    }
}
