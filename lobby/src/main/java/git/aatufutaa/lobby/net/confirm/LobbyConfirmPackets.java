package git.aatufutaa.lobby.net.confirm;

import git.aatufutaa.lobby.net.confirm.packet.common.LinkGameCenterIncomingPacket;
import git.aatufutaa.lobby.net.confirm.packet.common.LinkGameCenterOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.friend.*;
import git.aatufutaa.lobby.net.confirm.packet.hero.CancelHeroUpgradeOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.hero.UpgradeHeroIncomingPacket;
import git.aatufutaa.lobby.net.confirm.packet.hero.UpgradeHeroOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.heropass.*;
import git.aatufutaa.lobby.net.confirm.packet.misc.*;
import git.aatufutaa.lobby.net.confirm.packet.quests.CancelQuestOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.quests.QuestIncomingPacket;
import git.aatufutaa.lobby.net.confirm.packet.quests.QuestOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.queue.JoinQueueIncomingPacket;
import git.aatufutaa.lobby.net.confirm.packet.queue.LeaveQueueIncomingPacket;
import git.aatufutaa.lobby.net.confirm.packet.queue.LeaveQueueOutgoingPacket;
import git.aatufutaa.lobby.net.confirm.packet.shop.BuyShopItemIncomingPacket;
import git.aatufutaa.lobby.net.confirm.packet.shop.CancelPurchaseOutgoingPacket;
import git.aatufutaa.lobby.net.packet.misc.SetNameIncomingPacket;
import git.aatufutaa.lobby.net.packet.misc.SetNameOutgoingPacket;
import git.aatufutaa.lobby.party.player.*;
import git.aatufutaa.server.play.confirm.ConfirmPacketRegistry;

public class LobbyConfirmPackets {

    public static void register() {
        // queue
        ConfirmPacketRegistry.registerIncoming(0, JoinQueueIncomingPacket.class);
        ConfirmPacketRegistry.registerIncoming(1, LeaveQueueIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(1, LeaveQueueOutgoingPacket.class);

        // party
        ConfirmPacketRegistry.registerIncoming(2, CreatePartyIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(2, CreatePartyOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(3, JoinPartyIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(3, JoinPartyOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(4, LeavePartyIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(4, LeavePartyOutgoingPacket.class);

        ConfirmPacketRegistry.registerOutgoing(5, PlayerJoinPartyOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(6, PlayerLeavePartyOutgoingPacket.class);
        //registerOutgoing(7, PlayerUpdatePartyOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(7, SetNameIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(7, SetNameOutgoingPacket.class);

        //friends
        ConfirmPacketRegistry.registerIncoming(8, InviteFriendIncomingPacket.class);

        ConfirmPacketRegistry.registerOutgoing(8, AddFriendInviteOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(9, RemoveFriendInviteOutgoingPacket.class);

        ConfirmPacketRegistry.registerOutgoing(10, AddFriendOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(11, RemoveFriendOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(12, AcceptInviteIncomingPacket.class);

        ConfirmPacketRegistry.registerOutgoing(13, FriendResponseOutgoingPacket.class);

        ConfirmPacketRegistry.registerOutgoing(14, UpdateFriendOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(15, CancelFriendInviteIncomingPacket.class);

        ConfirmPacketRegistry.registerIncoming(16, RemoveFriendIncomingPacket.class);

        // misc
        ConfirmPacketRegistry.registerIncoming(17, RequestProfileIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(17, RequestProfileOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(18, ClaimLevelRewardIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(18, ClaimLevelRewardOutgoingPacket.class);

        ConfirmPacketRegistry.registerOutgoing(19, CancelRewardOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(20, RewardOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(21, UpgradeHeroIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(21, UpgradeHeroOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(22, CancelHeroUpgradeOutgoingPacket.class);

        ConfirmPacketRegistry.registerOutgoing(23, RemoveCoinsOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(24, UpdateSettingsIncomingPacket.class);

        ConfirmPacketRegistry.registerIncoming(25, QuestIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(25, QuestOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(26, CancelQuestOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(27, BuyShopItemIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(27, CancelPurchaseOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(28, ClaimProgressionIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(28, ClaimProgressionOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(29, CancelClaimProgressionOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(30, ClaimHeroPassRewardIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(30, ClaimHeroPassRewardOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(31, CancelHeroPassRewardOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(32, BuyNextTierHeroPassIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(32, BuyNextTierHeroPassOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(33, CancelBuyNextTierHeroPassOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(34, BuyHeroPassIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(34, BuyHeroPassOutgoingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(35, CancelBuyHeroPassOutgoingPacket.class);

        ConfirmPacketRegistry.registerIncoming(40, LinkGameCenterIncomingPacket.class);
        ConfirmPacketRegistry.registerOutgoing(40, LinkGameCenterOutgoingPacket.class);
    }
}
