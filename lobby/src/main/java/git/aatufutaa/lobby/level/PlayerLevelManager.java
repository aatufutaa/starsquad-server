package git.aatufutaa.lobby.level;

import git.aatufutaa.lobby.LobbyServer;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerLevelManager {

    @Getter
    private PlayerLevel[] levels;

    @Getter
    private Map<Integer, PlayerHero> heroes;

    @Getter
    private Map<Integer, PlayerProgression> progression;

    @Getter
    private PlayerHeroPassItem[] heroPassItems;
    @Getter
    private int season;
    @Getter
    private int heroPassPrice;

    @Getter
    private PlayerShop shop;

    private boolean running;

    public void start() {
        this.running = true;
        new Thread(this::update).start();
    }

    public void stop() {
        this.running = false;
    }

    public void update() {
        if (!this.running) return;

        try {

            Document document = LobbyServer.getInstance().getMongoManager().getUpdates().find(
                    new Document("_id", "level_" + LobbyServer.getInstance().getVersion())).first();

            if (document == null) {
                throw new Exception("cant find level");
            }

            List<Document> levels = document.getList("levels", Document.class);

            PlayerLevel[] playerLevels = new PlayerLevel[levels.size()];

            for (int i = 0; i < levels.size(); i++) {
                Document level = levels.get(i);

                int points = level.getInteger("points");

                Document reward = level.get("reward", Document.class);
                int type = reward.getInteger("type");
                int amount = reward.getInteger("amount");

                playerLevels[i] = new PlayerLevel(i + 1, points, type, amount);
            }

            document = LobbyServer.getInstance().getMongoManager().getUpdates().find(
                    new Document("_id", "heroes_" + LobbyServer.getInstance().getVersion())).first();

            if (document == null) {
                throw new Exception("cant find heroes");
            }

            List<Document> heroes = document.getList("heroes", Document.class);

            Map<Integer, PlayerHero> heroMap = new HashMap<>(heroes.size());
            for (Document document1 : heroes) {
                int id = document1.getInteger("id");
                int rarity = document1.getInteger("rarity");
                List<Document> heroLevels = document1.getList("levels", Document.class);
                List<PlayerHero.HeroLevel> heroLevels1 = new ArrayList<>();
                for (Document document2 : heroLevels) {
                    int expPrice = document2.getInteger("expPrice");
                    int coinPrice = document2.getInteger("coinPrice");
                    heroLevels1.add(new PlayerHero.HeroLevel(expPrice, coinPrice));
                }
                heroMap.put(id, new PlayerHero(id, rarity, heroLevels1));
            }

            document = LobbyServer.getInstance().getMongoManager().getUpdates().find(
                    new Document("_id", "progression_" + LobbyServer.getInstance().getVersion())).first();

            if (document == null) {
                throw new Exception("cant find progression");
            }

            List<Document> progressionRewards = document.getList("progression", Document.class);
            Map<Integer, PlayerProgression> progressionMap = new HashMap<>();
            for (Document doc : progressionRewards) {
                int id = doc.getInteger("id");
                int trophies = doc.getInteger("trophies");
                Document reward = doc.get("reward", Document.class);
                int rewardType = reward.getInteger("type");
                int rewardAmount = reward.getInteger("amount");
                PlayerProgression playerProgression = new PlayerProgression(id, trophies, rewardType, rewardAmount);
                progressionMap.put(id, playerProgression);
            }

            document = LobbyServer.getInstance().getMongoManager().getUpdates().find(
                    new Document("_id", "heropass_" + LobbyServer.getInstance().getVersion())).first();

            if (document == null) {
                throw new Exception("cant find hero_pass");
            }

            List<Document> heroPassRewards = document.getList("rewards", Document.class);
            PlayerHeroPassItem[] heroPassItems = new PlayerHeroPassItem[heroPassRewards.size()];
            for (int i = 0; i < heroPassRewards.size(); i++) {
                Document doc = heroPassRewards.get(i);

                int tokens = doc.getInteger("tokens");
                Document heroReward = doc.get("hero", Document.class);
                Document freeReward = doc.get("free", Document.class);

                int heroRewardType = heroReward.getInteger("type");
                int heroRewardAmount = heroReward.getInteger("amount");

                int freeRewardType = freeReward.getInteger("type");
                int freeRewardAmount = freeReward.getInteger("amount");

                PlayerHeroPassItem heroPassItem = new PlayerHeroPassItem(tokens, heroRewardType, heroRewardAmount, freeRewardType, freeRewardAmount);
                heroPassItems[i] = heroPassItem;
            }

            this.season = document.getInteger("season");
            this.heroPassPrice = document.getInteger("heroPassPrice");

            document = LobbyServer.getInstance().getMongoManager().getUpdates().find(
                    new Document("_id", "shop_" + LobbyServer.getInstance().getVersion())).first();

            if (document == null) {
                throw new Exception("cant find shop");
            }

            Document shopDocument = document.get("shop", Document.class);

            PlayerShop shop = new PlayerShop(
                    ((Number) shopDocument.get("commonManaAsGems")).floatValue(),
                    ((Number) shopDocument.get("epicManaAsGems")).floatValue(),
                    ((Number) shopDocument.get("legendaryManaAsGems")).floatValue(),
                    ((Number) shopDocument.get("coinAsGems")).floatValue(),
                    ((Number) shopDocument.get("heroTokenAsGems")).floatValue()
            );

            LobbyServer.getInstance().runOnMainThread(() -> {
                this.levels = playerLevels;
                this.heroes = heroMap;
                this.progression = progressionMap;
                this.heroPassItems = heroPassItems;
                this.shop = shop;
            });

        } catch (Exception e) {
            e.printStackTrace();
            LobbyServer.getInstance().crash("failed to get level data");
        }

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
        }
    }

    public PlayerLevel getLevelData(int points) {
        PlayerLevel level = null;
        for (PlayerLevel lvl : this.levels) {
            if (lvl.getPoints() <= points) {
                level = lvl;
            } else break;
        }
        return level;
    }

    public PlayerLevel getLevel(int level) {
        if (this.levels.length > level) return this.levels[level];
        return null;
    }
}
