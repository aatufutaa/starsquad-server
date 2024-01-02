package git.aatufutaa.login.net.packet;

import git.aatufutaa.login.LoginServer;
import git.aatufutaa.login.net.client.LoginClient;
import git.aatufutaa.login.net.client.ProtocolState;
import git.aatufutaa.login.redis.RedisTask;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

public class HelloIncomingPacket implements IncomingPacket {

    private int versionMajor; // server changes
    private int versionMinor; // client changes
    private boolean android; // for update

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.versionMajor = buf.readIntLE();
        this.versionMinor = buf.readIntLE();
        this.android = buf.readBoolean();
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        LoginClient loginClient = (LoginClient) client;
        loginClient.assertProtocolState(ProtocolState.HELLO);

        LoginServer.log("login hello version=" + this.versionMajor + " " + this.versionMinor + " android=" + android);

        RedisTask redisTask = LoginServer.getInstance().getRedisTask();

        if (redisTask.isRedisDown()) {
            client.sendPacketAndClose(HelloOutgoingPacket.fail("Login servers are currently down. Sorry!"));
            return;
        }

        loginClient.setVersion(this.versionMajor);
        loginClient.setAndroid(this.android);

        // current version
        if (this.versionMajor == redisTask.getCurrentVersionMajor()/* && this.versionMinor == redisTask.getCurrentVersionMinor()*/) {

            if (redisTask.isCurrentMasterDown()) {
                client.sendPacketAndClose(HelloOutgoingPacket.fail("Login servers are currently down. Sorry!"));
                return;
            }

            if (LoginServer.getInstance().getMaintenance().isEnabled()) {
                client.sendPacketAndClose(HelloOutgoingPacket.maintenance("Maintenance", LoginServer.getInstance().getMaintenance().getMsg()));
                return;
            }

            String url = redisTask.getCurrentAssetUrl();
            String assetVersion;

            if (this.android) {
                assetVersion = redisTask.getCurrentAndroidAssetVersion();
            } else {
                assetVersion = redisTask.getCurrentIosAssetVersion();
            }

            client.sendPacket(HelloOutgoingPacket.ok(url, assetVersion));
            loginClient.setProtocolState(ProtocolState.ENCRYPTION);
            return;
        }

        // beta
        if (this.versionMajor == redisTask.getBetaVersionMajor()/* && this.versionMinor == redisTask.getBetaVersionMinor()*/) {

            if (redisTask.isBetaMasterDown()) {
                client.sendPacketAndClose(HelloOutgoingPacket.fail("Login servers are currently down. Sorry!"));
                return;
            }

            client.sendPacket(new RequestAccessKeyOutgoingPacket());
            loginClient.setProtocolState(ProtocolState.REQUEST_ACCESS_KEY);
            return;
        }

        // send out of date
        client.sendPacketAndClose(HelloOutgoingPacket.update(this.android ? redisTask.getDownloadLinkAndroid() : redisTask.getDownloadLinkIos()));
    }
}
