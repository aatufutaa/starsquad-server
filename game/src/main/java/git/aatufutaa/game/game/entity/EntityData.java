package git.aatufutaa.game.game.entity;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityData {

    private int entityId;
    private float x;
    private float y;
    private float rot;

    public void write(ByteBuf buf) {
        buf.writeByte(this.entityId);
        buf.writeFloatLE(this.x);
        buf.writeFloatLE(this.y);
        buf.writeFloatLE(this.rot);
    }
}
