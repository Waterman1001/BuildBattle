/*
 * BuildBattle 3 - Ultimate building competition minigame
 * Copyright (C) 2018  Plajer's Lair - maintained by Plajer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.plajer.buildbattle3.arena;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.plajer.buildbattle3.ConfigPreferences;
import pl.plajer.buildbattle3.Main;
import pl.plajer.buildbattle3.VoteItems;
import pl.plajer.buildbattle3.buildbattleapi.BBGameChangeStateEvent;
import pl.plajer.buildbattle3.buildbattleapi.BBGameEndEvent;
import pl.plajer.buildbattle3.buildbattleapi.BBGameStartEvent;
import pl.plajer.buildbattle3.handlers.ChatManager;
import pl.plajer.buildbattle3.handlers.MessageHandler;
import pl.plajer.buildbattle3.language.LanguageManager;
import pl.plajer.buildbattle3.plots.Plot;
import pl.plajer.buildbattle3.plots.PlotManager;
import pl.plajer.buildbattle3.user.User;
import pl.plajer.buildbattle3.user.UserManager;
import pl.plajer.buildbattle3.utils.MessageUtils;
import pl.plajer.buildbattle3.utils.OptionsMenu;
import pl.plajer.buildbattle3.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Tom on 17/08/2015.
 */
public class Arena extends BukkitRunnable {

    public static Main plugin;
    private static List<String> themes = new ArrayList<>();
    private static List<Integer> blacklist = new ArrayList<>();
    private Map<Integer, UUID> topList = new HashMap<>();
    private String theme = "Theme";
    private PlotManager plotManager;
    private boolean receivedVoteItems;
    private Queue<UUID> queue = new LinkedList<>();
    private Random random = new Random();
    private int extraCounter;
    private Plot votingPlot = null;
    private boolean voteTime;
    private boolean scoreboardDisabled = ConfigPreferences.isScoreboardDisabled();
    private boolean bossBarEnabled = ConfigPreferences.isBarEnabled();
    private int BUILD_TIME = ConfigPreferences.getBuildTime();
    private boolean PLAYERS_OUTSIDE_GAME_ENABLED = ConfigPreferences.isHidePlayersOutsideGameEnabled();
    private int LOBBY_STARTING_TIMER = ConfigPreferences.getLobbyTimer();
    private boolean WIN_COMMANDS_ENABLED = ConfigPreferences.isWinCommandsEnabled();
    private boolean SECOND_PLACE_COMMANDS_ENABLED = ConfigPreferences.isSecondPlaceCommandsEnabled();
    private boolean THIRD_PLACE_COMMANDS_ENABLED = ConfigPreferences.isThirdPlaceCommandsEnabled();
    private boolean END_GAME_COMMANDS_ENABLED = ConfigPreferences.isEndGameCommandsEnabled();
    private ArenaState gameState;
    private int minimumPlayers = 2;
    private int maximumPlayers = 10;
    private String mapName = "";
    private int timer;
    private String ID;
    private boolean ready = true;
    private Location lobbyLoc = null;
    private Location startLoc = null;
    private Location endLoc = null;
    private Set<UUID> players = new HashSet<>();
    private BossBar gameBar;

    public Arena(String ID) {
        gameState = ArenaState.WAITING_FOR_PLAYERS;
        this.ID = ID;
        if(bossBarEnabled) {
            gameBar = Bukkit.createBossBar(ChatManager.colorMessage("Bossbar.Waiting-For-Players"), BarColor.BLUE, BarStyle.SOLID);
        }
        plotManager = new PlotManager(this);
    }

    /**
     * Adds new theme topic to the game
     *
     * @param string Theme topic
     */
    public static void addTheme(String string) {
        themes.add(string);
    }

    /**
     * Adds item which can not be used in game
     *
     * @param ID item ID to blacklist
     */
    public static void addToBlackList(int ID) {
        blacklist.add(ID);
    }

    /**
     * Checks if arena is validated and ready to play
     *
     * @return true = ready, false = not ready either you must validate it or it's wrongly created
     */
    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Is voting time in game?
     *
     * @return true = voting time, false = no
     */
    public boolean isVoting() {
        return voteTime;
    }

    void setVoting(boolean voting) {
        voteTime = voting;
    }

    public PlotManager getPlotManager() {
        return plotManager;
    }

    private void setRandomTheme() {
        setTheme(themes.get(random.nextInt(themes.size() - 1)));
    }

    public BossBar getGameBar() {
        return gameBar;
    }

    public int getBuildTime() {
        return BUILD_TIME;
    }

    Queue<UUID> getQueue() {
        return queue;
    }

    public void run() {
        //idle task
        if(getPlayers().size() == 0 && getArenaState() == ArenaState.WAITING_FOR_PLAYERS) return;
        if(!this.scoreboardDisabled) updateScoreboard();
        if(bossBarEnabled) {
            updateBossBar();
        }
        switch(getArenaState()) {
            case WAITING_FOR_PLAYERS:
                if(plugin.isBungeeActivated())
                    plugin.getServer().setWhitelist(false);
                getPlotManager().resetPlotsGradually();
                if(getPlayers().size() < getMinimumPlayers()) {
                    if(getTimer() <= 0) {
                        setTimer(LOBBY_STARTING_TIMER);
                        String message = ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players")
                                .replaceAll("%MINPLAYERS%", String.valueOf(getMinimumPlayers()));
                        for(Player p : getPlayers()) {
                            p.sendMessage(ChatManager.PLUGIN_PREFIX + message);
                        }
                        return;
                    }
                } else {
                    String message = ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Enough-Players-To-Start");
                    for(Player p : getPlayers()) {
                        p.sendMessage(ChatManager.PLUGIN_PREFIX + message);
                    }
                    setGameState(ArenaState.STARTING);
                    Bukkit.getPluginManager().callEvent(new BBGameStartEvent(this));
                    setTimer(LOBBY_STARTING_TIMER);
                    this.showPlayers();
                }
                setTimer(getTimer() - 1);
                break;
            case STARTING:
                if(getTimer() == 0) {
                    extraCounter = 0;
                    if(!getPlotManager().isPlotsCleared()) {
                        getPlotManager().resetQeuedPlots();
                    }
                    setGameState(ArenaState.IN_GAME);
                    getPlotManager().distributePlots();
                    getPlotManager().teleportToPlots();
                    setTimer(BUILD_TIME);
                    for(Player player : getPlayers()) {
                        player.getInventory().clear();
                        player.setGameMode(GameMode.CREATIVE);
                        if(PLAYERS_OUTSIDE_GAME_ENABLED) hidePlayersOutsideTheGame(player);
                        player.getInventory().setItem(8, OptionsMenu.getMenuItem());
                        //to prevent Multiverse chaning gamemode bug
                        Bukkit.getScheduler().runTaskLater(plugin, () -> player.setGameMode(GameMode.CREATIVE), 20);
                    }
                    setRandomTheme();
                    String message = ChatManager.colorMessage("In-Game.Messages.Lobby-Messages.Game-Started");
                    for(Player p : getPlayers()) {
                        p.sendMessage(ChatManager.PLUGIN_PREFIX + message);
                    }
                }
                setTimer(getTimer() - 1);
                break;
            case IN_GAME:
                if(plugin.isBungeeActivated()) {
                    if(getMaximumPlayers() <= getPlayers().size()) {
                        plugin.getServer().setWhitelist(true);
                    } else {
                        plugin.getServer().setWhitelist(false);
                    }
                }
                if(getPlayers().size() <= 1) {
                    String message = ChatManager.colorMessage("In-Game.Messages.Game-End-Messages.Only-You-Playing");
                    for(Player p : getPlayers()) {
                        p.sendMessage(ChatManager.PLUGIN_PREFIX + message);
                    }
                    setGameState(ArenaState.ENDING);
                    Bukkit.getPluginManager().callEvent(new BBGameEndEvent(this));
                    setTimer(10);
                }
                if((getTimer() == (4 * 60) || getTimer() == (3 * 60) || getTimer() == 5 * 60 || getTimer() == 30 || getTimer() == 2 * 60 || getTimer() == 60 || getTimer() == 15) && !this.isVoting()) {
                    String message = ChatManager.colorMessage("In-Game.Messages.Time-Left-To-Build").replaceAll("%FORMATTEDTIME%", Util.formatIntoMMSS(getTimer()));
                    for(Player p : getPlayers()) {
                        p.sendMessage(ChatManager.PLUGIN_PREFIX + message);
                    }
                }
                if(getTimer() != 0 && !receivedVoteItems) {
                    if(extraCounter == 1) {
                        extraCounter = 0;
                        for(Player player : getPlayers()) {
                            User user = UserManager.getUser(player.getUniqueId());
                            Plot buildPlot = (Plot) user.getObject("plot");
                            if(buildPlot != null) {
                                if(!buildPlot.isInFlyRange(player)) {
                                    player.teleport(buildPlot.getTeleportLocation());
                                    player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("In-Game.Messages.Cant-Fly-Outside-Plot"));
                                }
                            }
                        }
                    }
                    extraCounter++;
                } else if(getTimer() == 0 && !receivedVoteItems) {
                    for(Player player : getPlayers()) {
                        queue.add(player.getUniqueId());
                    }
                    for(Player player : getPlayers()) {
                        player.getInventory().clear();
                        VoteItems.giveVoteItems(player);
                    }
                    receivedVoteItems = true;
                    setTimer(1);
                } else if(getTimer() == 0 && receivedVoteItems) {
                    setVoting(true);
                    if(!queue.isEmpty()) {
                        if(getVotingPlot() != null) {
                            for(Player player : getPlayers()) {
                                getVotingPlot().setPoints(getVotingPlot().getPoints() + UserManager.getUser(player.getUniqueId()).getInt("points"));
                                UserManager.getUser(player.getUniqueId()).setInt("points", 0);
                            }
                        }
                        voteRoutine();
                    } else {
                        if(getVotingPlot() != null) {
                            for(Player player : getPlayers()) {
                                getVotingPlot().setPoints(getVotingPlot().getPoints() + UserManager.getUser(player.getUniqueId()).getInt("points"));
                                UserManager.getUser(player.getUniqueId()).setInt("points", 0);
                            }
                        }
                        calculateResults();
                        announceResults();
                        Plot winnerPlot = getPlotManager().getPlot(topList.get(1));

                        for(Player player : getPlayers()) {
                            player.teleport(winnerPlot.getTeleportLocation());
                        }
                        this.setGameState(ArenaState.ENDING);
                        Bukkit.getPluginManager().callEvent(new BBGameEndEvent(this));
                        setTimer(10);
                    }
                }
                setTimer(getTimer() - 1);
                break;
            case ENDING:
                if(plugin.isBungeeActivated())
                    plugin.getServer().setWhitelist(false);
                setVoting(false);
                for(Player player : getPlayers()) {
                    Util.spawnRandomFirework(player.getLocation());
                    showPlayers();
                }
                if(getTimer() <= 0) {
                    giveRewards();
                    teleportAllToEndLocation();
                    for(Player player : getPlayers()) {
                        if(bossBarEnabled) {
                            gameBar.removePlayer(player);
                        }
                        player.getInventory().clear();
                        UserManager.getUser(player.getUniqueId()).removeScoreboard();
                        player.setGameMode(GameMode.SURVIVAL);
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        player.getInventory().setArmorContents(null);
                        player.sendMessage(ChatManager.PLUGIN_PREFIX + ChatManager.colorMessage("Commands.Teleported-To-The-Lobby"));
                        UserManager.getUser(player.getUniqueId()).addInt("gamesplayed", 1);
                        if(plugin.isInventoryManagerEnabled()) {
                            plugin.getInventoryManager().loadInventory(player);
                        }
                        plotManager.getPlot(player).fullyResetPlot();
                    }
                    clearPlayers();
                    setGameState(ArenaState.RESTARTING);
                    if(plugin.isBungeeActivated()) {
                        for(Player player : plugin.getServer().getOnlinePlayers()) {
                            this.addPlayer(player);
                        }
                    }
                }
                setTimer(getTimer() - 1);
                break;
            case RESTARTING:
                setTimer(14);
                setVoting(false);
                receivedVoteItems = false;
                if(plugin.isBungeeActivated() && ConfigPreferences.getBungeeShutdown()) {
                    plugin.getServer().shutdown();
                }
                setGameState(ArenaState.WAITING_FOR_PLAYERS);
                topList.clear();
        }
    }

    private void hidePlayersOutsideTheGame(Player player) {
        for(Player players : plugin.getServer().getOnlinePlayers()) {
            if(getPlayers().contains(players)) continue;
            player.hidePlayer(players);
            players.hidePlayer(player);
        }
    }

    private void updateBossBar() {
        switch(getArenaState()) {
            case WAITING_FOR_PLAYERS:
                gameBar.setTitle(ChatManager.colorMessage("Bossbar.Waiting-For-Players"));
                break;
            case STARTING:
                gameBar.setTitle(ChatManager.colorMessage("Bossbar.Starting-In").replaceAll("%time%", String.valueOf(getTimer())));
                break;
            case IN_GAME:
                if(!isVoting()) {
                    gameBar.setTitle(ChatManager.colorMessage("Bossbar.Time-Left").replaceAll("%time%", String.valueOf(getTimer())));
                } else {
                    gameBar.setTitle(ChatManager.colorMessage("Bossbar.Vote-Time-Left").replaceAll("%time%", String.valueOf(getTimer())));
                }
                break;
        }
    }

    private void giveRewards() {
        if(WIN_COMMANDS_ENABLED) {
            for(String string : ConfigPreferences.getWinCommands()) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), string.replaceAll("%PLAYER%", plugin.getServer().getOfflinePlayer(topList.get(1)).getName()));
            }
        }
        if(SECOND_PLACE_COMMANDS_ENABLED) {
            if(topList.get(2) != null) {
                for(String string : ConfigPreferences.getSecondPlaceCommands()) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), string.replaceAll("%PLAYER%", plugin.getServer().getOfflinePlayer(topList.get(2)).getName()));
                }
            }
        }
        if(THIRD_PLACE_COMMANDS_ENABLED) {
            if(topList.get(3) != null) {
                for(String string : ConfigPreferences.getThirdPlaceCommands()) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), string.replaceAll("%PLAYER%", plugin.getServer().getOfflinePlayer(topList.get(3)).getName()));
                }
            }
        }
        if(END_GAME_COMMANDS_ENABLED) {
            for(String string : ConfigPreferences.getEndGameCommands()) {
                for(Player player : getPlayers()) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), string.replaceAll("%PLAYER%", player.getName()).replaceAll("%RANG%", Integer.toString(getRang(player))));
                }
            }
        }
    }

    private Integer getRang(Player player) {
        for(int i : topList.keySet()) {
            if(topList.get(i).equals(player.getUniqueId())) {
                return i;
            }
        }
        return 0;
    }

    public void start() {
        this.runTaskTimer(plugin, 20L, 20L);
    }

    private void updateScoreboard() {
        if(getPlayers().size() == 0 || getArenaState() == ArenaState.RESTARTING) return;
        for(Player p : getPlayers()) {
            ArenaBoard displayBoard = new ArenaBoard("BB3", "board", ChatManager.colorMessage("Scoreboard.Title"));
            List<String> lines = LanguageManager.getLocaleFile().getStringList("Scoreboard.Content." + getArenaState().getFormattedName());
            for(String line : lines) {
                displayBoard.addRow(formatScoreboardLine(line, p));
            }
            displayBoard.finish();
            displayBoard.display(p);
        }
    }

    private String formatScoreboardLine(String string, Player player) {
        String returnString = string;
        returnString = returnString.replaceAll("%MIN_PLAYERS%", Integer.toString(getMinimumPlayers()));
        returnString = returnString.replaceAll("%PLAYERS%", Integer.toString(getPlayers().size()));
        returnString = returnString.replaceAll("%PLAYER%", player.getName());
        returnString = returnString.replaceAll("%THEME%", getTheme());
        returnString = returnString.replaceAll("%MIN_PLAYERS%", Integer.toString(getMinimumPlayers()));
        returnString = returnString.replaceAll("%MAX_PLAYERS%", Integer.toString(getMaximumPlayers()));
        returnString = returnString.replaceAll("%TIMER%", Integer.toString(getTimer()));
        returnString = returnString.replaceAll("%TIME_LEFT%", Long.toString(getTimeLeft()));
        returnString = returnString.replaceAll("%FORMATTED_TIME_LEFT%", Util.formatIntoMMSS(getTimer()));
        returnString = returnString.replaceAll("%ARENA_ID%", getID());
        returnString = returnString.replaceAll("%MAPNAME%", getMapName());
        if(ConfigPreferences.isVaultEnabled()) {
            returnString = returnString.replaceAll("%MONEY%", Double.toString(Main.getEcon().getBalance(player)));
        }
        returnString = ChatManager.colorRawMessage(returnString);
        return returnString;
    }

    public List<Integer> getBlacklist() {
        return blacklist;
    }

    /**
     * Get arena's building time left
     *
     * @return building time left
     */
    public long getTimeLeft() {
        return getTimer();
    }

    /**
     * Get current arena theme
     *
     * @return arena theme String
     */
    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    private void voteRoutine() {
        if(!queue.isEmpty()) {
            setTimer(ConfigPreferences.getVotingTime());
            setTimer(ConfigPreferences.getVotingTime());
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(queue.poll());
            while(getPlotManager().getPlot(player.getUniqueId()) == null && !queue.isEmpty()) {
                System.out.print("A PLAYER HAS NO PLOT!");
                player = plugin.getServer().getPlayer(queue.poll());
            }
            if(queue.isEmpty() && getPlotManager().getPlot(player.getUniqueId()) == null) {
                setVotingPlot(null);
            } else {
                // getPlotManager().teleportAllToPlot(plotManager.getPlot(player.getUniqueId()));
                setVotingPlot(plotManager.getPlot(player.getUniqueId()));
                String message = ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Voting-For-Player-Plot").replaceAll("%PLAYER%", player.getName());
                for(Player p : getPlayers()) {
                    p.teleport(getVotingPlot().getTeleportLocation());
                    MessageHandler.sendTitleMessage(p, ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Plot-Owner-Title").replaceAll("%player%", player.getName()), 5, 40, 5);
                    p.sendMessage(ChatManager.PLUGIN_PREFIX + message);
                }
            }
        }

    }

    /**
     * Get plot where players are voting currently
     *
     * @return Plot object where players are voting
     */
    public Plot getVotingPlot() {
        return votingPlot;
    }

    private void setVotingPlot(Plot buildPlot) {
        votingPlot = buildPlot;
    }

    private void announceResults() {
        for(Player player : getPlayers()) {
            MessageHandler.sendTitleMessage(player, ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Title").replaceAll("%player%", plugin.getServer().getOfflinePlayer(topList.get(1)).getName()), 5, 40, 5);
            player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.Header"));
            player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.First-Winner")
                    .replaceAll("%player%", plugin.getServer().getOfflinePlayer(topList.get(1)).getName())
                    .replaceAll("%number%", String.valueOf(getPlotManager().getPlot(topList.get(1)).getPoints())));
            if(topList.containsKey(2) && topList.get(2) != null) {
                if(getPlotManager().getPlot(topList.get(1)).getPoints() == getPlotManager().getPlot(topList.get(2)).getPoints()) {
                    player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.First-Winner")
                            .replaceAll("%player%", plugin.getServer().getOfflinePlayer(topList.get(2)).getName())
                            .replaceAll("%number%", String.valueOf(getPlotManager().getPlot(topList.get(2)).getPoints())));
                } else {
                    player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.Second-Winner")
                            .replaceAll("%player%", plugin.getServer().getOfflinePlayer(topList.get(2)).getName())
                            .replaceAll("%number%", String.valueOf(getPlotManager().getPlot(topList.get(2)).getPoints())));
                }
            }
            if(topList.containsKey(3) && topList.get(3) != null) {
                if(getPlotManager().getPlot(topList.get(1)).getPoints() == getPlotManager().getPlot(topList.get(3)).getPoints()) {
                    player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.First-Winner")
                            .replaceAll("%player%", plugin.getServer().getOfflinePlayer(topList.get(3)).getName())
                            .replaceAll("%number%", String.valueOf(getPlotManager().getPlot(topList.get(3)).getPoints())));
                } else if(getPlotManager().getPlot(topList.get(2)).getPoints() == getPlotManager().getPlot(topList.get(3)).getPoints()) {
                    player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.Second-Winner")
                            .replaceAll("%player%", plugin.getServer().getOfflinePlayer(topList.get(3)).getName())
                            .replaceAll("%number%", String.valueOf(getPlotManager().getPlot(topList.get(3)).getPoints())));
                } else {
                    player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.Third-Winner")
                            .replaceAll("%player%", plugin.getServer().getOfflinePlayer(topList.get(3)).getName())
                            .replaceAll("%number%", String.valueOf(getPlotManager().getPlot(topList.get(3)).getPoints())));
                }
            }
            player.sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.Footer"));
        }
        for(Integer rang : topList.keySet()) {
            if(topList.get(rang) != null) {
                if(plugin.getServer().getPlayer(topList.get(rang)) != null) {
                    if(rang > 3) {
                        plugin.getServer().getPlayer(topList.get(rang)).sendMessage(ChatManager.colorMessage("In-Game.Messages.Voting-Messages.Winner-Message.You-Became-Other").replaceAll("%number%", String.valueOf(rang)));
                    }
                    if(rang == 1) {
                        UserManager.getUser(plugin.getServer().getPlayer(topList.get(rang)).getUniqueId()).addInt("wins", 1);
                        if(getPlotManager().getPlot(topList.get(rang)).getPoints() > UserManager.getUser(topList.get(rang)).getInt("highestwin")) {
                            UserManager.getUser(plugin.getServer().getPlayer(topList.get(rang)).getUniqueId()).setInt("highestwin", getPlotManager().getPlot(topList.get(rang)).getPoints());
                        }
                    } else {
                        UserManager.getUser(plugin.getServer().getPlayer(topList.get(rang)).getUniqueId()).addInt("loses", 1);
                    }
                }
            }
        }
    }

    private void calculateResults() {
        for(int b = 1; b <= 10; b++) {
            topList.put(b, null);
        }
        for(Plot buildPlot : getPlotManager().getPlots()) {
            long i = buildPlot.getPoints();
            Iterator it = topList.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Integer rang = (Integer) pair.getKey();
                if(topList.get(rang) == null || getPlotManager().getPlot(topList.get(rang)) == null) {
                    topList.put(rang, buildPlot.getOwner());
                    break;
                }
                if(i > getPlotManager().getPlot(topList.get(rang)).getPoints()) {
                    insertScore(rang, buildPlot.getOwner());
                    break;
                }
            }
        }
    }

    private void insertScore(int rang, UUID uuid) {
        UUID after = topList.get(rang);
        topList.put(rang, uuid);
        if(!(rang > 10) && after != null) insertScore(rang + 1, after);
    }

    /**
     * Get arena ID, ID != map name
     * ID is used to get and manage arenas
     *
     * @return arena ID
     */
    public String getID() {
        return ID;
    }

    /**
     * Min players that are required to start arena
     *
     * @return min players size
     */
    public int getMinimumPlayers() {
        return minimumPlayers;
    }

    public void setMinimumPlayers(int minimumPlayers) {
        this.minimumPlayers = minimumPlayers;
    }

    /**
     * Get map name, map name != ID
     * Map name is used in signs
     *
     * @return map name String
     */
    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapname) {
        this.mapName = mapname;
    }

    void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    void removePlayer(Player player) {
        if(player == null) return;
        if(player.getUniqueId() == null) return;
        players.remove(player.getUniqueId());
    }

    private void clearPlayers() {
        players.clear();
    }

    /**
     * Global timer of arena
     *
     * @return timer of arena
     */
    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    /**
     * Max players size arena can hold
     *
     * @return max players size
     */
    public int getMaximumPlayers() {
        return maximumPlayers;
    }

    public void setMaximumPlayers(int maximumPlayers) {
        this.maximumPlayers = maximumPlayers;
    }

    /**
     * Arena state of arena
     *
     * @return arena state
     * @see ArenaState
     */
    public ArenaState getArenaState() {
        return gameState;
    }

    /**
     * Changes arena state of arena
     * Calls BBGameChangeStateEvent
     *
     * @param gameState arena state to change
     * @see BBGameChangeStateEvent
     */
    public void setGameState(ArenaState gameState) {
        if(getArenaState() != null) {
            BBGameChangeStateEvent gameChangeStateEvent = new BBGameChangeStateEvent(gameState, this, getArenaState());
            plugin.getServer().getPluginManager().callEvent(gameChangeStateEvent);
        }
        this.gameState = gameState;
    }

    void showPlayers() {
        for(Player player : getPlayers()) {
            for(Player p : getPlayers()) {
                player.showPlayer(p);
                p.showPlayer(player);
            }
        }
    }

    /**
     * Get players in game
     *
     * @return HashSet with players
     */
    public HashSet<Player> getPlayers() {
        HashSet<Player> list = new HashSet<>();
        for(UUID uuid : players) {
            list.add(Bukkit.getPlayer(uuid));
        }

        return list;
    }

    void showPlayer(Player p) {
        for(Player player : getPlayers()) {
            player.showPlayer(p);
        }
    }

    public void teleportToLobby(Player player) {
        Location location = getLobbyLocation();
        if(location == null) {
            System.out.print("LobbyLocation isn't intialized for arena " + getID());
        }
        player.teleport(location);

    }

    /**
     * Lobby location of arena
     *
     * @return lobby loc of arena
     */
    public Location getLobbyLocation() {
        return lobbyLoc;
    }

    public void setLobbyLocation(Location loc) {
        this.lobbyLoc = loc;
    }

    /**
     * Start location of arena
     *
     * @return start loc of arena
     */
    public Location getStartLocation() {
        return startLoc;
    }

    private void teleportAllToEndLocation() {
        if(plugin.isBungeeActivated()) {
            for(Player player : getPlayers()) {
                plugin.getBungeeManager().connectToHub(player);
            }
            return;
        }
        Location location = getEndLocation();

        if(location == null) {
            location = getLobbyLocation();
            System.out.print("EndLocation for arena " + getID() + " isn't intialized!");
        }
        for(Player player : getPlayers()) {
            player.teleport(location);
        }
    }

    public void teleportToEndLocation(Player player) {
        if(plugin.isBungeeActivated()) {
            plugin.getBungeeManager().connectToHub(player);
            return;
        }
        Location location = getEndLocation();
        if(location == null) {
            location = getLobbyLocation();
            System.out.print("EndLocation for arena " + getID() + " isn't intialized!");
        }

        player.teleport(location);
    }

    /**
     * End location of arena
     *
     * @return end loc of arena
     */
    public Location getEndLocation() {
        return endLoc;
    }

    public void setEndLocation(Location endLoc) {
        this.endLoc = endLoc;
    }

}
