package git.aatufutaa.login.net.packet;

import git.aatufutaa.login.LoginServer;
import git.aatufutaa.login.net.client.LoginClient;
import git.aatufutaa.login.net.client.ProtocolState;
import git.aatufutaa.login.redis.RedisTask;
import git.aatufutaa.server.communication.ByteBufUtil;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class RequestAccessKeyIncomingPacket implements IncomingPacket {

    private String key;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.key = ByteBufUtil.readString(buf, 64);
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        LoginClient loginClient = (LoginClient)client;

        loginClient.assertProtocolState(ProtocolState.REQUEST_ACCESS_KEY);

        if (this.key.equals(LoginServer.getInstance().getRedisTask().getBetaAccessKey())) {
            LoginServer.warn(client + " invalid access key " + this.key);
            client.sendPacketAndClose(HelloOutgoingPacket.fail("Invalid access key!"));
            return;
        }

        RedisTask redisTask = LoginServer.getInstance().getRedisTask();

        String url = redisTask.getBetaAssetUrl();
        String assetVersion;

        if (loginClient.isAndroid()) {
            assetVersion = redisTask.getBetaAndroidAssetVersion();
        } else {
            assetVersion = redisTask.getBetaIosAssetVersion();
        }

        client.sendPacket(HelloOutgoingPacket.ok(url, assetVersion));
        loginClient.setProtocolState(ProtocolState.ENCRYPTION);
    }
}
