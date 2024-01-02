package git.aatufutaa.lobby.mongo;

import git.aatufutaa.lobby.LobbyServer;
import git.aatufutaa.lobby.hero.Hero;
import git.aatufutaa.lobby.level.PlayerHero;
import git.aatufutaa.lobby.level.PlayerLevel;
import git.aatufutaa.lobby.master.misc.AddFriendMasterIncomingPacket;
import git.aatufutaa.lobby.master.session.DisconnectMasterOutgoingPacket;
import git.aatufutaa.lobby.master.session.PlayerDataMasterOutgoingPacket;
import git.aatufutaa.lobby.master.tutorial.StartTutorialMasterOutgoingPacket;
import git.aatufutaa.lobby.quests.QuestData;
import git.aatufutaa.lobby.session.LobbyData;
import git.aatufutaa.lobby.session.Session;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import redis.clients.jedis.Jedis;

import java.util.*;

public class PlayerDataManager {

    private static final int CURRENT_VERSION = 0;

    private static Document createPlayerData(Session session) {
        Document document = new Document("_id", session.getPlayerId());

        document.put("version", CURRENT_VERSION);

        document.put("gems", 30);
        document.put("coins", 240);

        document.put("exp_common", 0);
        document.put("exp_rare", 0);
        document.put("exp_legendary", 0);

        document.put("tutorial_stage", "tutorial");

        document.put("total_rating", 0);

        document.put("name", "_");

        document.put("level_points", 0);

        document.put("heroes", List.of(createHeroDocument(0)));

        document.put("selected_hero", 0);

        LobbyServer.getInstance().getLobbyMongoManager().getPlayers().insertOne(document);

        LobbyServer.log("Player data created for " + session.getPlayerId());
        System.out.println(document);

        return document;
    }

    public static Document createHeroDocument(int id) {
        Document hero = new Document();
        hero.put("id", id);
        hero.put("level", 1);
        hero.put("trophies", 0);
        hero.put("highest_trophies", 0);
        hero.put("unlocked_at", System.currentTimeMillis());
        return hero;
    }

    public static void loadPlayerData(Session session) {
        int playerId = session.getPlayerId();

        LobbyServer.log("Loading player data for " + playerId);

        try (Jedis jedis = LobbyServer.getInstance().getRedisManager().getResource()) {

            PlayerDataCache.CacheResult res = PlayerDataCache.fetchData(jedis, playerId, true, true);

            Document document = res.document();
            if (res.error() == PlayerDataCache.CacheError.PLAY_NOT_FOUND) {
                LobbyServer.log("Player data not found for " + playerId + " creating");
                document = createPlayerData(session);
                PlayerDataCache.clearCache(jedis, playerId);
            } else if (res.error() != PlayerDataCache.CacheError.OK) {
                throw new Exception("cache error " + res.error());
            }

            String tutorialStage = document.getString("tutorial_stage");
            if (tutorialStage == null || tutorialStage.equals("tutorial")) {
                LobbyServer.log("Start tutorial");
                LobbyServer.getInstance().getMasterConnection().sendPacket(new StartTutorialMasterOutgoingPacket(playerId));
                return;
            }

            LobbyData lobbyData = session.getLobbyData();

            // id
            lobbyData.setPlayerId(PlayerId.convertIdToHash(playerId));

            // name
            String name = document.getString("name");
            lobbyData.setName(name);

            // level
            int levelPoints = document.getInteger("level_points");

            PlayerLevel level = LobbyServer.getInstance().getLevelManager().getLevelData(levelPoints);
            PlayerLevel nextLevel = LobbyServer.getInstance().getLevelManager().getLevel(level.getLevel());

            int currentProgress = levelPoints - level.getPoints();
            int maxProgress = nextLevel == null ? -1 : nextLevel.getPoints() - level.getPoints();

            int claimedLevelRewardIndex = document.getInteger("claimed_level_index", 0);

            lobbyData.setLevel(level.getLevel());
            lobbyData.setLevelProgress(currentProgress);
            lobbyData.setMaxLevelProgress(maxProgress);
            lobbyData.setClaimedLevelRewardIndex(claimedLevelRewardIndex);

            int heroTokens = document.getInteger("hero_tokens", 0);
            boolean hasHeroPass = document.containsKey("hero_pass_" + LobbyServer.getInstance().getLevelManager().getSeason());
            int heroPassHeroClaimIndex = document.getInteger("hero_pass_hero_claim_index", 0);
            int heroPassFreeClaimIndex = document.getInteger("hero_pass_free_claim_index", 0);
            lobbyData.setSeasonEndTime(9999);
            lobbyData.setHeroTokens(heroTokens);
            lobbyData.setHasHeroPass(hasHeroPass);
            lobbyData.setHeroPassHeroClaimIndex(heroPassHeroClaimIndex);
            lobbyData.setHeroPassFreeClaimIndex(heroPassFreeClaimIndex);

            // gems coins
            int gems = document.getInteger("gems");
            int coins = document.getInteger("coins");
            lobbyData.setGems(gems);
            lobbyData.setCoins(coins);

            int expCommon = document.getInteger("exp_common");
            int expRare = document.getInteger("exp_rare");
            int expLegendary = document.getInteger("exp_legendary");
            lobbyData.setExpCommon(expCommon);
            lobbyData.setExpRare(expRare);
            lobbyData.setExpLegendary(expLegendary);

            // heroes
            int totalTrophies = 0;
            int highestTrophies = 0;
            Map<Integer, Hero> heroes = new HashMap<>();
            List<Document> playerHeroes = document.getList("heroes", Document.class);
            for (Document document1 : playerHeroes) {
                int heroId = document1.getInteger("id");
                PlayerHero serverHero = LobbyServer.getInstance().getLevelManager().getHeroes().get(heroId);
                if (serverHero == null) continue;
                int heroLevel = document1.getInteger("level");
                int rating = document1.getInteger("trophies");
                int highestHeroTrophies = document1.getInteger("highest_trophies");
                heroes.put(heroId, new Hero(serverHero, heroLevel, rating));
                totalTrophies += rating;
                highestTrophies += highestHeroTrophies;
            }
            lobbyData.setHeroes(heroes);
            lobbyData.setTotalTrophies(totalTrophies);
            lobbyData.setHighestTotalTrophies(highestTrophies);

            int giveTrophies = document.getInteger("give_trophies", 0);
            if (totalTrophies < giveTrophies || giveTrophies < 0) giveTrophies = 0; // just in case givetrophies doesnt reset
            lobbyData.setGiveTrophies(giveTrophies);

            int giveTokens = document.getInteger("give_tokens", 0);
            if (heroTokens < giveTokens) giveTokens = 0;
            lobbyData.setGiveTokens(giveTokens);

            int selectedHero = document.getInteger("selected_hero");
            if (LobbyServer.getInstance().getLevelManager().getHeroes().get(selectedHero) != null) {
                lobbyData.setSelectedHero(selectedHero);
            }

            // friends
            // get id
            List<Integer> friendIdList = new ArrayList<>();
            if (document.containsKey("friends")) {
                List<Document> friendsList = document.getList("friends", Document.class);
                for (Document d : friendsList) {
                    int friendId = d.getInteger("player_id");
                    friendIdList.add(friendId);
                }
            }

            // set data
            Map<Integer, AddFriendMasterIncomingPacket.Friend> friends = new HashMap<>(friendIdList.size());
            lobbyData.setFriends(friends);

            // get data for friend
            for (int friendId : friendIdList) {
                AddFriendMasterIncomingPacket.Friend friend = getFriend(jedis, friendId);
                if (friend != null)
                    friends.put(friendId, friend);
            }

            // outgoing invites
            Map<Integer, AddFriendMasterIncomingPacket.Friend> outgoingInvites = new HashMap<>();
            lobbyData.setOutgoingInvites(outgoingInvites);

            Set<String> rawInviteList = jedis.smembers("outgoing_invites_" + playerId);
            if (rawInviteList != null) {
                for (String s : rawInviteList) {
                    int friendId = Integer.parseInt(s);
                    AddFriendMasterIncomingPacket.Friend friend = getFriend(jedis, friendId);
                    if (friend != null)
                        outgoingInvites.put(friendId, friend);
                    System.out.println("out invite " + friendId);
                }
            }

            // incoming invite
            Map<Integer, AddFriendMasterIncomingPacket.Friend> incomingInvites = new HashMap<>();
            lobbyData.setIncomingInvites(incomingInvites);

            rawInviteList = jedis.smembers("incoming_invites_" + playerId);
            if (rawInviteList != null) {
                for (String s : rawInviteList) {
                    int friendId = Integer.parseInt(s);
                    AddFriendMasterIncomingPacket.Friend friend = getFriend(jedis, friendId);
                    if (friend != null)
                        incomingInvites.put(friendId, friend);
                    System.out.println("inco invite " + friend);
                }
            }

            lobbyData.setAllowFriendRequests(document.getBoolean("allow_friend_requests", true));

            Map<Integer, QuestData> questsMap = new HashMap<>();
            if (document.containsKey("quests")) {
                List<Document> quests = document.getList("quests", Document.class);
                for (Document doc1 : quests) {
                    int id = doc1.getInteger("id");
                    int amount = doc1.getInteger("amount");
                    int claimIndex = doc1.getInteger("claim_index");
                    questsMap.put(id, new QuestData(id, amount, claimIndex));
                }
            }
            lobbyData.setQuestsMap(questsMap);

            Set<Integer> claimedProgression = new HashSet<>(document.getList("claimed_progression", Integer.class, Collections.emptyList()));
            lobbyData.setClaimedProgression(claimedProgression);

            if (giveTrophies > 0 || giveTokens > 0) {
                LobbyServer.getInstance().getLobbyMongoManager().getPlayers().updateOne(new Document("_id", session.getPlayerId()),
                        Updates.combine(
                                Updates.set("give_trophies", 0),
                                Updates.set("give_tokens", 0)
                        )
                );
            }

            System.out.println("data loaded");

            System.out.println("FRIeNDS REQUESTED " + session.isRequestFriends());
            if (session.isRequestFriends()) {
                LobbyServer.getInstance().getMasterConnection().sendPacket(new PlayerDataMasterOutgoingPacket(playerId, friendIdList));
            }

            LobbyServer.getInstance().runOnMainThread(session::onDataLoaded);

        } catch (Exception e) {
            e.printStackTrace();

            LobbyServer.warn("Cant load player data for " + playerId);
            LobbyServer.getInstance().getMasterConnection().sendPacket(new DisconnectMasterOutgoingPacket(playerId));

            session.kick("Failed to load your profile. Try again later.");
        }
    }

    private static AddFriendMasterIncomingPacket.Friend getFriend(Jedis jedis, int playerId) {
        PlayerDataCache.CacheResult friendData = PlayerDataCache.fetchData(jedis, playerId, false, false);
        if (friendData.error() != PlayerDataCache.CacheError.OK) return null;
        Document friendDocument = friendData.document();
        String friendPlayerIdHash = PlayerId.convertIdToHash(playerId);
        String friendName = friendDocument.getString("name");
        int friendRating = friendDocument.getInteger("total_rating");
        AddFriendMasterIncomingPacket.Friend friend = new AddFriendMasterIncomingPacket.Friend(friendPlayerIdHash, friendName, friendRating);

        return friend;
    }
}
