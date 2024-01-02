package git.aatufutaa.master.queue;

import git.aatufutaa.master.MasterServer;
import git.aatufutaa.master.party.Party;
import git.aatufutaa.master.server.PlayServer;
import git.aatufutaa.master.server.ServerLocation;
import git.aatufutaa.master.server.game.GameListener;
import git.aatufutaa.master.server.lobby.packet.game.SendToGameOutgoingPacket;
import git.aatufutaa.master.server.lobby.packet.queue.LeaveQueueOutgoingPacket;
import git.aatufutaa.master.server.lobby.packet.queue.QueueStatusOutgoingPacket;
import git.aatufutaa.master.session.Session;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GameQueue {

    private ScheduledExecutorService thread;

    private final QueueType queueType;

    private final LinkedList<QueueData> players = new LinkedList<>();

    //private final QueueMatchIndex[] indexes = new QueueMatchIndex[10];

    //private final LinkedList<TempMatch> matches = new LinkedList<>();

    private final LinkedList<QueueMatch> matches = new LinkedList<>();

    private final Random random = new Random();

    private final ServerLocation serverLocation;

    public static class QueueTeam {

        @Getter private LinkedList<QueueData> players = new LinkedList<>();

        private int count;

        public void add(QueueData queueData) {
            this.players.add(queueData);
            this.count += queueData.getPlayerCount();
        }

        public int getHeroCount(QueueData queueData) {
            // TODO: check if has smae hero
            return 0;
        }

    }

    public class QueueMatch {

        private LinkedList<QueueData> players;
        private boolean removed;

        private final int rating;
        private int waitingTime;

        private int bots;

        public int getTotalPlayers() {
            int count = 0;
            for (QueueData queueData : this.players) {
                count += queueData.getPlayerCount();
            }
            return count;
        }

        public QueueMatch(QueueData queueData) {
            this.players = new LinkedList<>();
            this.players.add(queueData);
            this.rating = queueData.getRating();
        }

        private boolean canStart() {
            return this.getTotalPlayers() + this.bots >= GameQueue.this.queueType.getMaxPlayers();
        }

        private boolean shouldAddBot() {
            double botChance = 0.8; // 0.8 fills up pretty quick 0-10s
            double ratingMultiplier = Math.min(this.rating / 1000.0, 1f); // if more than 1000 then no
            botChance += 0.1 * ratingMultiplier; // the higher rating (max 1000) the higher chance to not get bots
            // 0.9 takes about 30-40s

            return this.waitingTime > 5 && GameQueue.this.random.nextDouble() > botChance;
        }

        public void tick() {
            if (this.removed) return;

            ++this.waitingTime;

            Iterator<QueueData> iterator = this.players.iterator();
            while (iterator.hasNext()) {
                QueueData queueData = iterator.next();
                if (queueData.isLeft()) {

                    MasterServer.getInstance().getSessionManager().runOnSessionThread(queueData.getSession().getPlayerId(), (callback) -> {
                        queueData.getSession().setQueueData(null);
                        queueData.getSession().sendPacket(new LeaveQueueOutgoingPacket(queueData.getSession().getPlayerId(), false));
                    });

                    if (queueData.getMembers() != null) {
                        for (Session member : queueData.getMembers()) {
                            MasterServer.getInstance().getSessionManager().runOnSessionThread(member.getPlayerId(), (callback) -> {
                                member.setQueueData(null);
                                member.sendPacket(new LeaveQueueOutgoingPacket(member.getPlayerId(), false));
                            });
                        }
                    }

                    iterator.remove();
                }
            }

            if (this.players.isEmpty())
                this.removed = true;
            else {
                // add bots after 5s
                if (this.shouldAddBot()||true) {
                    ++this.bots;
                }

                if (this.canStart()) {
                    this.removed = true;

                    this.startMatch();
                }
            }

            this.broadcastUpdates(); // TODO: only when uidpate
        }

        private void broadcastUpdates() {
            int maxPlayers = GameQueue.this.queueType.getMaxPlayers();
            int players = Math.min(this.getTotalPlayers() + this.bots, maxPlayers);

            for (QueueData queueData : this.players) {
                queueData.getSession().sendPacket(new QueueStatusOutgoingPacket(queueData.getSession().getPlayerId(),
                        players, maxPlayers, false));

                if (queueData.getMembers() != null) {
                    for (Session member : queueData.getMembers()) {
                        member.sendPacket(new QueueStatusOutgoingPacket(member.getPlayerId(), players, maxPlayers, true));
                    }
                }
            }
        }

        private void cancel() {
            this.removed = true;

            for (QueueData queueData : this.players) {
                queueData.forEach(session -> {
                    MasterServer.getInstance().getSessionManager().runOnSessionThread(session.getPlayerId(), (callback) -> {
                        session.setQueueData(null);

                        session.sendPacket(new SendToGameOutgoingPacket(SendToGameOutgoingPacket.SendType.FAILED, session.getPlayerId()));
                        session.sendPacket(new LeaveQueueOutgoingPacket(session.getPlayerId(), false));
                    });
                });
            }
        }

        private void startMatch() {
            LinkedList<QueueTeam> teams = this.buildTeams(this.players, false);

            MasterServer.getInstance().getServerManager(GameQueue.this.serverLocation).getGameWithLeastPlayers(server -> {

                if (server == null) {
                    this.cancel();
                    return;
                }

                for (QueueData queueData : this.players) {
                    queueData.forEach(server::addPlayer);
                }

                server.startTeamGame(GameQueue.this.queueType, teams, new GameListener() {

                    @Override
                    public void onStarted() {

                        for (QueueTeam team : teams) {
                            for (QueueData queueData : team.players) {
                                queueData.forEach(session -> {
                                    MasterServer.getInstance().getSessionManager().runOnSessionThread(session.getPlayerId(), (callback) -> {

                                        session.setQueueData(null);

                                        PlayServer oldServer = session.getServer();

                                        session.setServer(server);

                                        if (oldServer != null) {

                                            oldServer.sendPacket(new SendToGameOutgoingPacket(SendToGameOutgoingPacket.SendType.OK, session.getPlayerId(), server.getHost(), server.getPort()));

                                            /*&& oldServer != server*/ // TODO: if joined queue from game
                                            MasterServer.getInstance().getServerManager(oldServer.getServerLocation()).runOnServerThread(() -> {
                                                oldServer.removePlayer(session);
                                            });
                                        }
                                    });
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancel() {
                        QueueMatch.this.cancel();

                        for (QueueTeam team : teams) {
                            for (QueueData queueData : team.players) {
                                queueData.forEach(server::removePlayer);
                            }
                        }
                    }
                });
            });
        }

        /*
        only 1 party per team!!!
        because 1 -> there should be games where 3 players are max per team and if you have a party you have min 2 players and 2+2=4 which is over team size
         */
        private LinkedList<QueueTeam> buildTeams(LinkedList<QueueData> players, boolean avoidSameHeroes) {

            int parties = 0;
            for (QueueData queueData : players) {
                if (queueData.getMembers() != null) ++parties;
            }

            if (parties > GameQueue.this.queueType.getTeamCount()) return null; // too many parties cant fit team!

            // size check ends here

            LinkedList<QueueTeam> teams = new LinkedList<>();
            for (int i = 0; i < GameQueue.this.queueType.getTeamCount(); i++) {
                teams.add(new QueueTeam());
            }

            // add parties
            int teamIndex = 0;
            for (QueueData queueData : players) {
                if (queueData.getMembers() != null) {
                    teams.get(teamIndex).add(queueData);
                    ++teamIndex;
                }
            }

            // now just fill the rest with players
            for (QueueData queueData : players) {
                if (queueData.getMembers() != null) continue;

                QueueTeam best = null;
                int heroCount = 0;

                for (QueueTeam team : teams) {
                    if (team.count >= GameQueue.this.queueType.getTeamSize()) continue; // team is full
                    int teamHeroCount = team.getHeroCount(queueData);
                    if (best == null || teamHeroCount < heroCount) {
                        best = team;
                        heroCount = teamHeroCount;
                    }
                }

                // if best team and still has same heroes dont add
                if (avoidSameHeroes && heroCount > 0) {
                    return null;
                }

                best.add(queueData);
            }

            return teams;
        }

        private int getRatingRange() {
            return 50 + Math.max(this.waitingTime * 10, 300); // +10 per sec -> 10s waited = +150 range (max 350) // TODO: increase max range if higher rating (for example 2000 rating -> 500)
        }

        private boolean isInRatingRange(QueueMatch match) {
            int ratingRange = this.getRatingRange();
            int minRating = Math.max(this.rating - ratingRange, 0);
            int maxRating = this.rating + ratingRange;
            return match.rating >= minRating && match.rating <= maxRating;
        }

        private boolean checkForSameHeroes() {
            return this.waitingTime < 10; // if less than 10s waited dont add same heroes
        }

        public void merge(QueueMatch match) {
            // check if in rating range
            if (!this.isInRatingRange(match) || !match.isInRatingRange(this)) return;

            // if there are space in match
            if (this.getTotalPlayers() + match.getTotalPlayers() > GameQueue.this.queueType.getMaxPlayers()) return;

            // if there are enough space in teams to fit parties
            LinkedList<QueueData> merged = new LinkedList<>(this.players);
            merged.addAll(match.players);

            boolean checkForSameHeroes = this.checkForSameHeroes() || match.checkForSameHeroes();

            LinkedList<QueueTeam> teams = this.buildTeams(merged, checkForSameHeroes);

            if (teams == null) {
                return;
            }

            int currentPlayers = this.players.size() + this.bots;
            int matchPlayers = match.players.size() + match.bots;

            int playersToShow = Math.max(currentPlayers, matchPlayers);
            this.bots = Math.max(playersToShow - merged.size(), 0);

            this.players = merged;
            match.removed = true;
        }
    }

    public GameQueue(QueueType queueType, ServerLocation serverLocation) {
        this.queueType = queueType;
        this.serverLocation = serverLocation;

        // for (int i = 0; i < this.indexes.length; i++) {
        //    this.indexes[i] = new QueueMatchIndex(queueType);
        // }
    }

    public void start() {
        this.thread = Executors.newSingleThreadScheduledExecutor();
        this.thread.scheduleAtFixedRate(this::tick, 0L, 1L, TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.thread != null)
            this.thread.shutdown();
    }

    private void tick() {
        // find match for added players
        Iterator<QueueData> iterator = this.players.iterator();
        while (iterator.hasNext()) {
            //this.findMatch(iterator.next());
            QueueMatch queueMatch = new QueueMatch(iterator.next());
            this.matches.add(queueMatch);

            iterator.remove();
        }

        // tick matches
        Iterator<QueueMatch> iterator1 = this.matches.iterator();
        while (iterator1.hasNext()) {
            QueueMatch match = iterator1.next();
            match.tick();
            if (match.removed)
                iterator1.remove();
        }

        // find players
        for (QueueMatch match : this.matches) {
            if (match.removed) continue;
            for (QueueMatch match1 : this.matches) {
                if (match1 == match) continue;
                if (match1.removed) continue;
                match.merge(match1);
            }
        }
        /*for (TempMatch match : this.matches){
            match.tick();
        }*/

        // merge matches
        //this.mergeMatches();
    }

    /*private void mergeMatches() {
        for (TempMatch match : this.matches) {
            if (match.isRemoved()) continue;
            for (TempMatch match2 : this.matches) {
                if (match == match2) continue;
                if (match2.isRemoved()) continue;
                if (!match.canMerge(match2)) continue;

                match.merge(match2);
                break;
            }
        }
        this.matches.removeIf(TempMatch::isRemoved);
    }*/

    /*private void findMatch(QueueData queueData) {
        // find best match if possible
        TempMatch best = null;
        int ratingIndex = 0;
        for (TempMatch match : this.matches) {
            if (match.canJoin(queueData)) {
                int newIndex = match.getRatingIndex(queueData);
                if (best == null || newIndex < ratingIndex) {
                    best = match;
                }
            }
        }

        // best match ! join
        if (best != null) {
            best.join(queueData);
            return;
        }

        // cant find a match! create one

        TempMatch match = new TempMatch(queueData, this.queueType);

        this.matches.add(match);
    }*/

    /*private void findMatch(QueueData queueData) {
        int ratingIndex = queueData.getRatingIndex();

        int min = Math.max(ratingIndex - 1, 0);
        int max = Math.min(ratingIndex + 1, 10);

        for (int i = min; i <= max; i++) {
            QueueMatchIndex index = this.indexes[i];

            if (index.findMatch(queueData)) return;
        }

        // no match find create one!

        QueueMatchIndex current = this.indexes[ratingIndex];
        current.createMatch(queueData);
    }*/

    public void add(Session session) {
        // on player thread

        // check if already in queue
        if (session.getQueueData() != null) {
            System.out.println(session + " already in queue");
            return;
        }

        Party party = session.getParty();

        // check if leader
        if (party != null && party.getLeader() != session) {
            System.out.println("only leader can join");
            return;
        }

        // check if party is ready
        if (party != null) {
            AtomicBoolean ready = new AtomicBoolean();// better way to do this exists???
            party.lock(() -> {
                ready.set(party.isReady());
            });
            if (!ready.get()) {
                // TODO: send party not ready
                System.out.println("party not ready");
                return;
            }
        }

        // set queue data to player
        QueueData queueData = new QueueData(500, session);
        session.setQueueData(queueData);

        Runnable done = () -> {

            int playerCount = 1;

            // send queue status to members
            if (queueData.getMembers() != null) {
                playerCount += queueData.getMembers().size();
                for (Session member : queueData.getMembers()) {
                    member.sendPacket(
                            new QueueStatusOutgoingPacket(member.getPlayerId(),
                                    playerCount,
                                    this.queueType.getMaxPlayers(),
                                    true));
                }
            }

            // send queue status to session
            session.sendPacket(new QueueStatusOutgoingPacket(session.getPlayerId(),
                    playerCount,
                    this.queueType.getMaxPlayers(),
                    false));

            this.thread.execute(() -> {

                this.players.add(queueData);

            });
        };

        if (party != null) {
            party.lock(() -> {
                int count = party.getMembers().size();

                if (count == 1) {
                    done.run();
                    return;
                }

                // TODO: get highest rating from mebers and set to queue data

                AtomicInteger added = new AtomicInteger(count - 1);
                Session[] members = new Session[count]; // this is safe way to do this i suppose

                for (int i = 0; i < count; i++) {
                    Session member = party.getMembers().get(i);

                    if (member == session) continue;

                    int index = i;

                    MasterServer.getInstance().getSessionManager().runOnSessionThread(member.getPlayerId(), (callback) -> {
                        boolean ready = added.decrementAndGet() == 0;

                        if (member.getQueueData() != null) {
                            System.out.println("member already in queue");
                        } else {
                            member.setQueueData(queueData);
                            members[index] = member;
                        }

                        if (ready) {

                            List<Session> membersList = new ArrayList<>();
                            for (Session addedMember : members) {
                                if (addedMember == null) continue;
                                membersList.add(addedMember);
                            }
                            queueData.setMembers(membersList);

                            done.run();
                        }
                    });
                }
            });
        } else {
            done.run();
        }
    }
}
