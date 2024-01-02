package git.aatufutaa.lobby.session;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.master.misc.UpdateFriendStatusMasterIncomingPacket;
import git.aatufutaa.lobby.master.session.PlayerOnlineMasterOutgoingPacket;
import git.aatufutaa.lobby.mongo.PlayerId;
import git.aatufutaa.lobby.net.LobbyClient;
import git.aatufutaa.lobby.net.confirm.packet.friend.UpdateFriendOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.quests.QuestOutgoingPacket;
import git.aatufutaa.lobby.net.packet.hello.LobbyDataOutgoingPacket;
import git.aatufutaa.lobby.party.Party;
import git.aatufutaa.lobby.party.master.JoinPartyMasterIncomingPacket;
import git.aatufutaa.lobby.party.player.PlayerJoinPartyOutgoingPacket;
import git.aatufutaa.lobby.quests.QuestData;
import git.aatufutaa.lobby.quests.QuestType;
import git.aatufutaa.server.play.session.SessionBase;
import com.mongodb.client.model.Updates;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Getter
public class Session extends SessionBase {

    private byte[] key;

    @Setter
    private SessionState sessionState = SessionState.NOT_LOADED;

    private final LobbyData lobbyData = new LobbyData();

    @Setter
    private Party party;
    @Setter
    private boolean sentPartyCreateRequest;

    @Setter
    private boolean inQueue;

    @Setter
    private boolean updatingName;

    @Getter
    @Setter
    private boolean requestFriends;

    @Getter
    private final Map<Integer, UpdateFriendStatusMasterIncomingPacket.FriendStatus> friendStatus = new HashMap<>();

    @Getter
    @Setter
    public boolean modifyData;

    @Getter
    @Setter
    private int location;

    public Session(int playerId, String secret, byte[] key) {
        super(playerId, secret);
        this.key = key;
    }

    public void update(byte[] key) {
        this.key = key;
    }

    public void onConnect(LobbyClient client) {
        this.client = client;
    }

    public boolean isConnected() {
        return this.client != null;
    }

    // called when channel closes
    public void onDisconnect(LobbyClient client) {
        if (this.client == client) { // if channel didnt close because new client connected
            this.client.getChannel().close(); // should be already closed
            this.client = null;

            LobbyServer.getInstance().getMasterConnection().sendPacket(new PlayerOnlineMasterOutgoingPacket(this.playerId, false));
        }
    }

    public void onDataLoaded() {
        this.sessionState = SessionState.LOADED;
        this.sendData();
    }

    public void updateQuest(QuestType questType, int amount, Consumer<Boolean> accept) {
        LobbyServer.getInstance().getAsyncThread().execute(() -> {
            AtomicBoolean failed = new AtomicBoolean(false);

            QuestData questData = this.lobbyData.getQuestsMap().get(questType.getId());
            boolean create = questData == null;

            try {
                if (create) {
                    LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(
                            new Document("_id", this.playerId),
                            Updates.addToSet("quests", new Document("id", questType.getId())
                                    .append("amount", amount)
                                    .append("claim_index", 0))
                    );
                } else {
                    LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(
                            new Document("_id", this.playerId).append("quests.id", questType.getId()),
                            Updates.set("quests.$.amount", amount));
                }
            } catch (Exception e) {
                failed.set(false);
                e.printStackTrace();
            }

            LobbyServer.getInstance().runOnMainThread(() -> {
                if (failed.get()) {
                    accept.accept(true);
                    return;
                }

                QuestData data;
                if (create) {
                    data = new QuestData(questType.getId(), amount, 0);
                    this.lobbyData.getQuestsMap().put(data.getId(), data);
                } else {
                    data = questData;
                    questData.setAmount(amount);
                }

                this.sendConfirmPacket(new QuestOutgoingPacket(questType.getId(), amount, data.getClaimIndex()));

                accept.accept(false);
            });
        });
    }

    public void sendData() {
        LobbyDataOutgoingPacket packet = new LobbyDataOutgoingPacket();

        packet.setLobbyData(this.lobbyData.clone());

        packet.setInQueue(this.inQueue);

        boolean inParty = this.party != null;
        packet.setInParty(inParty);
        if (inParty) {
            packet.setPartyCode(this.party.getPartyCode());
            packet.setPartyLeaderId(PlayerId.convertIdToHash(this.party.getLeaderId()));
            List<PlayerJoinPartyOutgoingPacket.PartyMember> members = new ArrayList<>(this.party.getMembers().size());
            for (JoinPartyMasterIncomingPacket.MasterPartyMember member : this.party.getMembers().values()) {
                if (member.getPlayerId() == this.playerId) continue; // dont add the player itself
                members.add(member.toPartyMember());
            }
            packet.setMembers(members);
        }

        packet.setLocation(this.location);

        this.sendPacket(packet);

        if (!this.friendStatus.isEmpty()) {
            UpdateFriendOutgoingPacket.FriendStatusUpdate[] updates = new UpdateFriendOutgoingPacket.FriendStatusUpdate[this.friendStatus.size()];
            int i = 0;
            for (Map.Entry<Integer, UpdateFriendStatusMasterIncomingPacket.FriendStatus> entry : this.friendStatus.entrySet()) {
                updates[i++] = new UpdateFriendOutgoingPacket.FriendStatusUpdate(PlayerId.convertIdToHash(entry.getKey()), entry.getValue());
            }
            this.sendConfirmPacket(new UpdateFriendOutgoingPacket(updates));
        }

        if (this.inQueue) {
            // TODO: send status
        }

        this.lobbyData.setGiveTrophies(0);
        this.lobbyData.setGiveTokens(0);
    }
}
