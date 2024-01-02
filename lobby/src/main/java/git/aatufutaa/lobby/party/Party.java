package git.aatufutaa.lobby.party;

import git.aatufutaa.lobby.party.master.JoinPartyMasterIncomingPacket;
import git.aatufutaa.lobby.util.Hashids;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Party {

    private static final Hashids HASH_ID = new Hashids("OV3KQTTtecKD1VnawxjGpIz78uimlHsx", 8); // TODO: get this from master and change it

    private final int partyId;
    private final int leaderId;

    private final String partyCode;

    private final Map<Integer, JoinPartyMasterIncomingPacket.MasterPartyMember> members = new HashMap<>();

    public Party(int partyId, int leaderId) {
        this.partyId = partyId;
        this.leaderId = leaderId;
        this.partyCode = getPartyCode(partyId);
    }

    public static String getPartyCode(int partyId) {
        return HASH_ID.encode(partyId); // convert party id to hash
    }

    public static int getPartyId(String partyCode) {
        return (int) HASH_ID.decode(partyCode.toUpperCase())[0]; // convert hash to int
    }

    public void join(JoinPartyMasterIncomingPacket.MasterPartyMember partyMember) {
        this.members.put(partyMember.getPlayerId(), partyMember);
    }

    public boolean leave(int playerId) {
        return this.members.remove(playerId) != null;
    }
}
