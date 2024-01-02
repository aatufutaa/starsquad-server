package git.aatufutaa.lobby.net.packet.misc;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.mongo.PlayerDataCache;
import git.aatufutaa.lobby.net.packet.LobbyPacket;
import git.aatufutaa.lobby.session.Session;
import git.aatufutaa.server.communication.ByteBufUtil;
import com.mongodb.client.model.Updates;
import io.netty.buffer.ByteBuf;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.Set;

public class SetNameIncomingPacket extends LobbyPacket {

    private String name;

    @Override
    public void read(ByteBuf buf) throws Exception {
        this.name = ByteBufUtil.readString(buf, 16);
    }

    @Override
    protected void handle0(Session session) throws Exception {

        if (session.isUpdatingName()) {
            session.sendConfirmPacket(new SetNameOutgoingPacket(SetNameOutgoingPacket.NameResponse.INSERT_FAILED, null));
            return;
        }

        if (session.getLobbyData().getName().equals("_")) { // no name changed

            session.setUpdatingName(true);

            LobbyServer.getInstance().getAsyncThread().execute(() -> {

                // check for characters
                SetNameOutgoingPacket.NameResponse response = this.testFilter(this.name);

                if (response != SetNameOutgoingPacket.NameResponse.OK) {
                    session.sendConfirmPacket(new SetNameOutgoingPacket(response, null));
                    return;
                }

                // update name to database
                try {
                    LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(
                            new Document("_id", session.getPlayerId()),
                            Updates.set("name", this.name)
                    );
                    try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {
                        PlayerDataCache.clearCache(jedis, session.getPlayerId());
                    } catch (Exception e) {
                        throw e;
                    }

                    LobbyServer.getInstance().runOnMainThread(() -> {
                        session.setUpdatingName(false);

                        session.getLobbyData().setName(this.name);
                        session.sendConfirmPacket(new SetNameOutgoingPacket(SetNameOutgoingPacket.NameResponse.OK, this.name));
                    });

                } catch (Exception e) {
                    e.printStackTrace();

                    LobbyServer.getInstance().runOnMainThread(() -> {
                        session.setUpdatingName(false);
                        session.sendConfirmPacket(new SetNameOutgoingPacket(SetNameOutgoingPacket.NameResponse.INSERT_FAILED, null));
                    });
                }
            });

        } else {
            // TODO: check if allowed to change name
        }
    }

    private static boolean checkCharacters(String input) {
        for (int i = 0; i < input.length(); i++) {
            int c = input.charAt(i);
            if (c > 0x7F) {
                return false;
            }
        }

        return true;
    }

    private final static Set<String> badWords = new HashSet<>();

    static {
        // TODO: read from a file
    }

    private static boolean checkForBadWords(String input) {
        String fixedInput = input.replace("0", "o")
                .replace("1", "i")
                .replace("4", "a")
                .replace("5", "s")
                .replace("7", "t")
                .replace("8", "b")
                .toLowerCase();

        for (String badWord : badWords) {
            if (input.contains(badWord)) return false;
            if (fixedInput.contains(badWord)) return false;
        }

        return true;
    }

    private SetNameOutgoingPacket.NameResponse testFilter(String name) {
        if (name.length() < 3) return SetNameOutgoingPacket.NameResponse.INVALID_OTHER;
        if (name.length() > 16) return SetNameOutgoingPacket.NameResponse.INVALID_OTHER;
        if (!checkCharacters(name)) return SetNameOutgoingPacket.NameResponse.INVALID_CHARACTERS;
        if (!checkForBadWords(name)) return SetNameOutgoingPacket.NameResponse.NOT_ALLOWED;

        return SetNameOutgoingPacket.NameResponse.OK;
    }
}
