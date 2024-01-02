package git.aatufutaa.game.master.packet;

import git.aatufutaa.server.communication.packet.MasterOutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StartGameMasterOutgoingPacket implements MasterOutgoingPacket {

    public enum StartType {
        OK,
        FAIL,
        STATE_PLAY,
        STATE_ENDING,
        STATE_ENDED
    }

    private int gameId;
    private StartType startType;

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.gameId);
        buf.writeByte(this.startType.ordinal());
    }
}
