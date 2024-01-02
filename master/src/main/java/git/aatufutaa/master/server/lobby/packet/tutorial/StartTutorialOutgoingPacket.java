package git.aatufutaa.master.server.lobby.packet.tutorial;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.communication.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class StartTutorialOutgoingPacket implements OutgoingPacket {

    public enum TutorialState {
        OK,
        CANT_FIND_SERVER,
        FAIL
    }

    private TutorialState state;
    private int playerId;

    private String host;
    private int port;

    public StartTutorialOutgoingPacket(TutorialState tutorialState, int playerId) {
        this.state = tutorialState;
        this.playerId = playerId;
    }

    @Override
    public void write(ByteBuf buf) throws Exception {
        buf.writeByte(this.state.ordinal());
        buf.writeInt(this.playerId);
        if (this.state == TutorialState.OK) {
            ByteBufUtil.writeString(this.host, buf);
            buf.writeInt(this.port);
        }
    }
}
