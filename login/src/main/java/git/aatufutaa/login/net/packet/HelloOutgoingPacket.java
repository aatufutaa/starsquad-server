package git.aatufutaa.login.net.packet;

import com.google.gson.JsonObject;
import git.aatufutaa.login.net.signature.SignatureManager;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HelloOutgoingPacket implements OutgoingPacket {

    private enum HelloResponse {
        OK,
        MAINTENANCE,
        UPDATE,
        FAIL
    }

    private final HelloResponse response;
    private final String msg;
    private final String signature;

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(this.response.ordinal());
        ByteBufUtil.writeString(this.msg, buf);
        buf.writeBoolean(this.signature != null);
        if (this.signature != null) {
            ByteBufUtil.writeString(this.signature, buf);
        }
    }

    public static HelloOutgoingPacket ok(String url, String assetFile) {
        JsonObject json = new JsonObject();
        json.addProperty("url", url);
        json.addProperty("assetFile", assetFile);

        String msg = json.toString();
        String signature = SignatureManager.sign(msg);

        return new HelloOutgoingPacket(HelloResponse.OK, msg, signature);
    }

    public static HelloOutgoingPacket maintenance(String title, String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("title", title);
        json.addProperty("msg", msg);

        return new HelloOutgoingPacket(HelloResponse.MAINTENANCE, json.toString(), null);
    }

    public static HelloOutgoingPacket update(String url) {
        JsonObject json = new JsonObject();
        json.addProperty("url", url);

        String msg = json.toString();
        String signature = SignatureManager.sign(msg);

        return new HelloOutgoingPacket(HelloResponse.UPDATE, msg, signature);
    }

    public static HelloOutgoingPacket fail(String msg) {
        JsonObject json = new JsonObject();
        json.addProperty("msg", msg);
        return new HelloOutgoingPacket(HelloResponse.FAIL, json.toString(), null);
    }
}
