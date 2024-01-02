package git.aatufutaa.master.server.login.packet;

import git.aatufutaa.master.communication.ByteBufUtil;
import git.aatufutaa.master.server.ServerType;
import io.netty.buffer.ByteBuf;

public class LoginOutgoingPacket extends RequestOutgoingPacket {

    public enum LoginResponse {
        OK,
        FAIL
    }

    private LoginResponse response;

    private String secret;

    private ServerType serverType;
    private String host;
    private int port;

    public LoginOutgoingPacket(int requestId, String secret, ServerType serverType, String host, int port) {
        super(requestId);
        this.response = LoginResponse.OK;
        this.secret = secret;
        this.serverType = serverType;
        this.host = host;
        this.port = port;
    }

    public LoginOutgoingPacket(int requestId, String msg) {
        super(requestId);
        this.response = LoginResponse.FAIL;
        this.secret = msg;
    }

    @Override
    public void write(ByteBuf buf) throws Exception {
        super.write(buf);
        buf.writeByte(this.response.ordinal());
        ByteBufUtil.writeString(this.secret, buf);
        if (this.response == LoginResponse.OK) {
            buf.writeByte(this.serverType.ordinal());
            ByteBufUtil.writeString(this.host, buf);
            buf.writeInt(this.port);
        }
    }
}
