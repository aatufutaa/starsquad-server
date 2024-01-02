package git.aatufutaa.lobby.session;

import git.aatufutaa.lobby.hero.Hero;
import git.aatufutaa.lobby.master.misc.AddFriendMasterIncomingPacket;
import git.aatufutaa.lobby.quests.QuestData;
import git.aatufutaa.server.communication.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class LobbyData implements Cloneable {

    private String playerId;
    private String name;

    private int level;
    private int levelProgress;
    private int maxLevelProgress;
    private int claimedLevelRewardIndex;

    private int seasonEndTime;
    private int heroTokens;
    private boolean hasHeroPass;
    private int heroPassHeroClaimIndex;
    private int heroPassFreeClaimIndex;

    private int gems;
    private int coins;

    private int expCommon;
    private int expRare;
    private int expLegendary;

    private Map<Integer, Hero> heroes;
    private int selectedHero;

    private int totalTrophies;
    private int highestTotalTrophies;

    private int giveTrophies;
    private int giveTokens;

    private Map<Integer, AddFriendMasterIncomingPacket.Friend> friends;
    private Map<Integer, AddFriendMasterIncomingPacket.Friend> outgoingInvites;
    private Map<Integer, AddFriendMasterIncomingPacket.Friend> incomingInvites;

    private List<AddFriendMasterIncomingPacket.Friend> partyInvites;

    private boolean allowFriendRequests;

    private Map<Integer, QuestData> questsMap;

    private Set<Integer> claimedProgression;

    public void write(ByteBuf buf) {
        boolean askForName = this.name.equals("_");
        buf.writeBoolean(askForName);
        if (!askForName)
            ByteBufUtil.writeString(this.name, buf);

        buf.writeByte(this.level);
        buf.writeShortLE(this.levelProgress);
        buf.writeShortLE(this.maxLevelProgress);
        buf.writeByte(this.claimedLevelRewardIndex);

        buf.writeIntLE(this.seasonEndTime);
        buf.writeShortLE(this.heroTokens);
        buf.writeBoolean(this.hasHeroPass);
        if (this.hasHeroPass) {
            buf.writeByte(this.heroPassHeroClaimIndex);
        }
        buf.writeByte(this.heroPassFreeClaimIndex);

        buf.writeIntLE(this.gems);
        buf.writeShortLE(this.coins);

        buf.writeShortLE(this.expCommon);
        buf.writeShortLE(this.expRare);
        buf.writeShortLE(this.expLegendary);

        buf.writeByte(this.heroes.size());
        for (Hero hero : this.heroes.values()) {
            hero.write(buf);
        }

        buf.writeByte(this.selectedHero);

        buf.writeShortLE(this.totalTrophies);
        buf.writeShortLE(this.highestTotalTrophies);

        buf.writeShortLE(this.giveTrophies);
        buf.writeShortLE(this.giveTokens);

        buf.writeByte(this.friends.size());
        for (AddFriendMasterIncomingPacket.Friend friend : this.friends.values()) {
            friend.write(buf);
        }

        buf.writeByte(this.outgoingInvites.size());
        for (AddFriendMasterIncomingPacket.Friend friend : this.outgoingInvites.values()) {
            friend.write(buf);
        }

        buf.writeByte(this.incomingInvites.size());
        for (AddFriendMasterIncomingPacket.Friend friend : this.incomingInvites.values()) {
            friend.write(buf);
        }

        //buf.writeByte(this.partyInvites.size());
        //for (AddFriendMasterIncomingPacket.Friend friend : this.partyInvites) {
        //    friend.write(buf);
        //}

        buf.writeBoolean(this.allowFriendRequests);

        buf.writeByte(this.questsMap.size());
        for (QuestData questData : this.questsMap.values()) {
            buf.writeByte(questData.getId());
            buf.writeShortLE(questData.getAmount());
            buf.writeByte(questData.getClaimIndex());
        }

        buf.writeByte(this.claimedProgression.size());
        for (int i : this.claimedProgression) {
            buf.writeByte(i);
        }
    }

    public int getExp(int rarity) {
        return switch (rarity) {
            case 0 -> this.expCommon;
            case 1 -> this.expRare;
            case 2 -> this.expLegendary;
            default -> 0;
        };
    }

    public void setExp(int rarity, int exp) {
        switch (rarity) {
            case 0 -> this.expCommon = exp;
            case 1 -> this.expRare = exp;
            case 2 -> this.expLegendary = exp;
        }
    }

    @Override
    public LobbyData clone() {
        try {
            LobbyData clone = (LobbyData) super.clone();

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
