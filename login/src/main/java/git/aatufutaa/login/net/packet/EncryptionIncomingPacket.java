package git.aatufutaa.login.net.packet;

import git.aatufutaa.login.LoginServer;
import git.aatufutaa.login.net.client.LoginClient;
import git.aatufutaa.login.net.client.ProtocolState;
import git.aatufutaa.login.net.encryption.RsaEncryption;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import io.netty.buffer.ByteBuf;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EncryptionIncomingPacket implements IncomingPacket {

    private byte[] key;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.key = new byte[256];
        buf.readBytes(this.key);
    }

    @Override
    public void handle(ClientBase client) throws Exception {
        LoginClient loginClient = (LoginClient)client;

        loginClient.assertProtocolState(ProtocolState.ENCRYPTION);

        loginClient.setProtocolState(ProtocolState.ENCRYPTING); // only 1 packet

        LoginServer.log("Key received");

        LoginServer.getInstance().getExecutor().execute(() -> {

            byte[] decryptedKey;
            try {
                decryptedKey = RsaEncryption.decrypt(this.key);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                     IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                client.getChannel().close();

                LoginServer.warn("Cant decrypt key sent by " + client);
                return;
            }

            if (decryptedKey.length != 16) {
                client.getChannel().close();

                LoginServer.warn("Received wrong key size " + client);
                return;
            }

            loginClient.setKey(decryptedKey);

            client.sendPacket(new EncryptionOutgoingPacket(), callback -> {
                LoginServer.log("Encryption enabled for " + client);

                client.enableEncryption(decryptedKey);
                loginClient.setProtocolState(ProtocolState.LOGIN);
            });
        });
    }
}
