package git.aatufutaa.login.master;

import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.communication.packet.packets.WaitForResponseIncomingPacket;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

@Getter
public class LoginMasterIncomingPacket extends WaitForResponseIncomingPacket {

    public enum LoginResponse {
        OK,
        FAIL
    }

    private LoginResponse response;

    private String secret;

    private ServerType serverType;
    private String host;
    private int port;

    @Override
    public void read(ByteBuf buf) throws Exception {
        super.read(buf);
        this.response = LoginResponse.values()[buf.readByte()];
        if (this.response == LoginResponse.OK) {
            this.secret = ByteBufUtil.readString(buf, 32);
            this.serverType = ServerType.values()[buf.readByte()];
            this.host = ByteBufUtil.readString(buf, 20);
            this.port = buf.readInt();
        } else {
            this.secret = ByteBufUtil.readString(buf, 120);
        }
    }

    @Override
    public void handle() {
    }
}
