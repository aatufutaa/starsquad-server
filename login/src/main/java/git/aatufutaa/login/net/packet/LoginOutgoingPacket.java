package git.aatufutaa.login.net.packet;

import git.aatufutaa.server.ServerType;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginOutgoingPacket implements OutgoingPacket {

    public enum LoginResponse {
        OK,
        SAVE_TOKEN,
        BANNED,
        FAIL
    }

    private LoginResponse response;
    private String msg; // secret, ban reason, fail reason

    private String playerId;

    private ServerInfo serverInfo;

    @AllArgsConstructor
    public static class ServerInfo {
        private ServerType serverType;
        private String host;
        private int port;
    }

    public LoginOutgoingPacket(LoginResponse response, String msg) {
        this.response = response;
        this.msg = msg;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
        ByteBufUtil.writeString(this.msg, buf);

        if (this.response == LoginResponse.OK) {
            ByteBufUtil.writeString(this.playerId, buf);

            buf.writeByte(this.serverInfo.serverType.ordinal());
            ByteBufUtil.writeString(this.serverInfo.host, buf);
            buf.writeShortLE(this.serverInfo.port);
        }
    }
}
