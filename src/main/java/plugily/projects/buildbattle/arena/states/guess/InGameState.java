/*
 *
 * BuildBattle - Ultimate building competition minigame
 * Copyright (C) 2021 Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
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
 *
 */

package plugily.projects.buildbattle.arena.states.guess;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import plugily.projects.buildbattle.arena.BaseArena;
import plugily.projects.buildbattle.arena.GuessArena;
import plugily.projects.buildbattle.arena.managers.plots.Plot;
import plugily.projects.buildbattle.handlers.themes.BBTheme;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.states.PluginInGameState;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.misc.MiscUtils;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.inventory.common.item.SimpleClickableItem;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Tigerpanzer_02
 * <p>Created at 28.05.2022
 */
public class InGameState extends PluginInGameState {

  private final Random random = new Random();

  @Override
  public void handleCall(PluginArena arena) {
    super.handleCall(arena);
    GuessArena pluginArena = (GuessArena) getPlugin().getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    switch(pluginArena.getArenaInGameStage()) {
      case THEME_VOTING:
        if(pluginArena.getCurrentBuilder() == null) {
          Player nextPlayer = pluginArena.getNextPlayerByRound();

          if(nextPlayer != null) {
            pluginArena.setCurrentBuilder(nextPlayer);
            Plot plot = pluginArena.getPlotManager().getPlot(nextPlayer);
            Location plotLoc = plot == null ? null : plot.getTeleportLocation();
            for(Player player : arena.getPlayers()) {
              if(plot != null) {
                if(plotLoc != null) {
                  player.teleport(plotLoc);
                }
                player.setPlayerWeather(plot.getWeatherType());
                player.setPlayerTime(Plot.Time.format(plot.getTime(), player.getWorld().getTime()), false);
              }
              VersionUtils.setCollidable(player, false);
              player.setGameMode(GameMode.ADVENTURE);
              player.setAllowFlight(true);
              player.setFlying(true);
              player.getInventory().clear();
              player.updateInventory();
            }
            if(plot != null) {
              Bukkit.getScheduler().runTaskLater(getPlugin(), () -> plot.getMembers().forEach(player -> player.setGameMode(GameMode.CREATIVE)), 40);
            }
          }
          new TitleBuilder("IN_GAME_MESSAGES_PLOT_GTB_THEME_BEING_SELECTED").asKey().arena(pluginArena).sendArena();
          openThemeSelectionInventoryToCurrentBuilder(pluginArena);
          break;
        }

        if(arena.getTimer() <= 0 || pluginArena.isCurrentThemeSet()) {
          forceSetTheme(pluginArena);

          new MessageBuilder("IN_GAME_MESSAGES_PLOT_GTB_ROUND").asKey().integer(pluginArena.getRound()).arena(pluginArena).sendArena();
          new TitleBuilder("IN_GAME_MESSAGES_PLOT_GTB_THEME_GUESS_TITLE").asKey().arena(pluginArena).sendArena();

          pluginArena.getCurrentBuilder().getInventory().setItem(8, pluginArena.getPlugin().getSpecialItemManager().getSpecialItem("OPTIONS_MENU").getItemStack());
          setArenaTimer(getPlugin().getConfig().getInt("Time-Manager." + pluginArena.getArenaType().getPrefix() + ".In-Game"));
          pluginArena.setArenaInGameStage(BaseArena.ArenaInGameStage.BUILD_TIME);
          /* from build arena
          if(pluginArena.getVotePoll() != null) {
            pluginArena.setTheme(pluginArena.getVotePoll().getVotedTheme());
          }
          */
          break;
        }
        break;
      case BUILD_TIME:
        // check not needed anymore
        // if(pluginArena.isCurrentThemeSet()) {
        if(arena.getTimer() <= 90) {
          if(arena.getTimer() == 90) {
            new MessageBuilder("IN_GAME_MESSAGES_PLOT_GTB_THEME_CHARS").asKey().arena(pluginArena).integer(pluginArena.getTheme().length()).sendArena();
          }
          sendThemeHints(arena, pluginArena);
        }
        if(arena.getTimer() <= 0) {
          //not all guessed
          new MessageBuilder("IN_GAME_MESSAGES_PLOT_GTB_THEME_WAS").asKey().arena(pluginArena).sendArena();
          new TitleBuilder("IN_GAME_MESSAGES_PLOT_GTB_THEME_TITLE").asKey().arena(pluginArena).sendArena();

          setArenaTimer(getPlugin().getConfig().getInt("Time-Manager." + pluginArena.getArenaType().getPrefix() + ".Round-Delay"));
          pluginArena.setArenaInGameStage(BaseArena.ArenaInGameStage.PLOT_VOTING);
        }
        //}
        handleBuildTime(pluginArena);
        break;
      case PLOT_VOTING:
        if(pluginArena.getRound() + 1 > pluginArena.getPlayersLeft().size()) {
          calculateResults(pluginArena);
          announceResults(pluginArena);
          Plot winnerPlot = pluginArena.getPlotList().get(pluginArena.getWinner());
          Location winnerLocation = winnerPlot.getTeleportLocation();

          for(Player player : pluginArena.getPlayers()) {
            player.teleport(winnerLocation);
            new TitleBuilder("IN_GAME_MESSAGES_PLOT_VOTING_WINNER").asKey().player(player).value(pluginArena.getWinner().getName()).sendPlayer();
          }
          getPlugin().getArenaManager().stopGame(false, arena);
        }
//round delay
        if(arena.getTimer() <= 0) {
          pluginArena.getRemovedCharsAt().clear();
          pluginArena.setCurrentBuilder(null);
          pluginArena.setCurrentTheme(null);
          pluginArena.getWhoGuessed().clear();
          setArenaTimer(getPlugin().getConfig().getInt("Time-Manager." + pluginArena.getArenaType().getPrefix() + ".Voting.Theme"));
          pluginArena.setArenaInGameStage(BaseArena.ArenaInGameStage.THEME_VOTING);
        }
        break;
      default:
        break;
    }
    if(arena.getTimer() <= 0) {
      getPlugin().getArenaManager().stopGame(false, arena);
    }
    // no players - stop game
    if(pluginArena.enoughPlayersToContinue()) {
      getPlugin().getArenaManager().stopGame(false, pluginArena);
    }
  }

  private void sendThemeHints(PluginArena arena, GuessArena pluginArena) {
    for(Player player : arena.getPlayers()) {
      if(pluginArena.getCurrentBuilder() == player) {
        continue;
      }
      if(pluginArena.getWhoGuessed().contains(player)) {
        VersionUtils.sendActionBar(player, pluginArena.getTheme());
        continue;
      }
      int themeLength = pluginArena.getCurrentTheme().getTheme().length();
      List<Integer> charsAt = new ArrayList<>(themeLength);

      for(int i = 0; i < themeLength; i++) {
        if(!pluginArena.getRemovedCharsAt().contains(i)) {
          charsAt.add(i);
        }
      }

      if(themeLength - pluginArena.getRemovedCharsAt().size() > 2) {
        if(arena.getTimer() % 10 == 0 && arena.getTimer() <= 70) {
          pluginArena.getRemovedCharsAt().add(charsAt.get(charsAt.size() == 1 ? 0 : random.nextInt(charsAt.size())));
          continue;
        }
      }

      StringBuilder actionbar = new StringBuilder();
      for(int i = 0; i < themeLength; i++) {
        char charAt = pluginArena.getCurrentTheme().getTheme().charAt(i);

        if(Character.isWhitespace(charAt)) {
          actionbar.append("  ");
          continue;
        }
        if(pluginArena.getRemovedCharsAt().contains(i)) {
          actionbar.append(charAt).append(' ');
          continue;
        }
        actionbar.append("_ ");
      }

      VersionUtils.sendActionBar(player, actionbar.toString());
    }
  }

  private void forceSetTheme(GuessArena pluginArena) {
    if(!pluginArena.isCurrentThemeSet()) {
      BBTheme.Difficulty difficulty = BBTheme.Difficulty.EASY;
      switch(random.nextInt(2 + 1)) {
        case 1:
          difficulty = BBTheme.Difficulty.MEDIUM;
          break;
        case 2:
          difficulty = BBTheme.Difficulty.HARD;
          break;
        default:
          break;
      }
      List<String> themes = pluginArena.getPlugin().getThemeManager().getThemes(BaseArena.ArenaType.GUESS_THE_BUILD.getPrefix() + "_" + difficulty.name());

      if(!themes.isEmpty()) {
        BBTheme theme = new BBTheme(themes.get(themes.size() == 1 ? 0 : random.nextInt(themes.size())), difficulty);
        pluginArena.setCurrentTheme(theme);
        VersionUtils.sendActionBar(pluginArena.getCurrentBuilder(), new MessageBuilder("IN_GAME_MESSAGES_PLOT_GTB_THEME_NAME").asKey().arena(pluginArena).build());
      }
      pluginArena.getCurrentBuilder().closeInventory();
    }
  }

  private void openThemeSelectionInventoryToCurrentBuilder(GuessArena pluginArena) {
    if(pluginArena.getCurrentBuilder() == null) {
      return;
    }

    NormalFastInv gui = new NormalFastInv(9 * 3, new MessageBuilder("MENU_THEME_GTB_INVENTORY").asKey().build());
    gui.addClickHandler(inventoryClickEvent -> inventoryClickEvent.setCancelled(true));
    gui.addCloseHandler(event -> {
      if(!pluginArena.isCurrentThemeSet()) {
        Bukkit.getScheduler().runTask(getPlugin(), () -> event.getPlayer().openInventory(event.getInventory()));
      }
    });
    List<String> themes = pluginArena.getPlugin().getThemeManager().getThemes(BaseArena.ArenaType.GUESS_THE_BUILD.getPrefix() + "_EASY");
    String itemLore = new MessageBuilder("MENU_THEME_GTB_ITEM_LORE").asKey().arena(pluginArena).build();
    String themeItemName = new MessageBuilder("MENU_THEME_GTB_ITEM_NAME").asKey().arena(pluginArena).build();
    String themeNameEasy = !themes.isEmpty() ? themes.get(themes.size() == 1 ? 0 : random.nextInt(themes.size())) : "";
    setTheme(pluginArena, themeNameEasy, BBTheme.Difficulty.EASY);
    gui.setItem(11, new SimpleClickableItem(new ItemBuilder(Material.PAPER).name(themeItemName)
        .lore(itemLore
            .replace("%difficulty%", new MessageBuilder("MENU_THEME_GTB_DIFFICULTIES_EASY").asKey().build())
            .replace("%points%", "1").split(";")).build(), event -> {

    }));


    themes = pluginArena.getPlugin().getThemeManager().getThemes(BaseArena.ArenaType.GUESS_THE_BUILD.getPrefix() + "_MEDIUM");
    String themeNameMedium = !themes.isEmpty() ? themes.get(themes.size() == 1 ? 0 : random.nextInt(themes.size())) : "";
    setTheme(pluginArena, themeNameMedium, BBTheme.Difficulty.MEDIUM);
    gui.setItem(13, new SimpleClickableItem(new ItemBuilder(Material.PAPER).name(themeItemName)
        .lore(itemLore
            .replace("%difficulty%", new MessageBuilder("MENU_THEME_GTB_DIFFICULTIES_MEDIUM").asKey().build())
            .replace("%points%", "2").split(";")).build(), event -> {

    }));

    themes = pluginArena.getPlugin().getThemeManager().getThemes(BaseArena.ArenaType.GUESS_THE_BUILD.getPrefix() + "_HARD");
    String themeNameHard = !themes.isEmpty() ? themes.get(themes.size() == 1 ? 0 : random.nextInt(themes.size())) : "";
    setTheme(pluginArena, themeNameHard, BBTheme.Difficulty.HARD);
    gui.setItem(15, new SimpleClickableItem(new ItemBuilder(Material.PAPER).name(themeItemName)
        .lore(itemLore
            .replace("%difficulty%", new MessageBuilder("MENU_THEME_GTB_DIFFICULTIES_HARD").asKey().build())
            .replace("%points%", "3").split(";")).build(), event -> {

    }));
    gui.open(pluginArena.getCurrentBuilder());

  }

  private void setTheme(GuessArena pluginArena, String themeName, BBTheme.Difficulty difficulty) {
    BBTheme theme = new BBTheme(ChatColor.stripColor(themeName), difficulty);
    pluginArena.setCurrentTheme(theme);
    pluginArena.getCurrentBuilder().closeInventory();
  }


  private void calculateResults(GuessArena pluginArena) {
    pluginArena.recalculateLeaderboard();
    List<Player> list = new ArrayList<>(pluginArena.getPlayersPoints().keySet());
    setWinner(pluginArena, list);
    for(int i = 1; i <= list.size(); i++) {
      getPlugin().getRewardsHandler().performReward(list.get(i - 1), pluginArena, getPlugin().getRewardsHandler().getRewardType("PLACE"), i);
    }
    getPlugin().getRewardsHandler().performReward(pluginArena, getPlugin().getRewardsHandler().getRewardType("END_GAME"));
  }

  private void setWinner(GuessArena pluginArena, List<Player> players) {
    pluginArena.setWinner(players.get(0));
  }


  private void announceResults(GuessArena pluginArena) {
    List<Player> list = new ArrayList<>(pluginArena.getPlayersPoints().keySet());

    for(int i = 1; i <= list.size(); i++) {
      Player player = list.get(i);
      User user = getPlugin().getUserManager().getUser(player);
      if(i > 3) {
        user.adjustStatistic("LOSES", 1);
      }
      user.adjustStatistic("WINS", 1);
      getPlugin().getUserManager().addExperience(player, 5);
      //todo gtb stats
    }
  }

  private void handleBuildTime(GuessArena pluginArena) {
    for(int timers : getPlugin().getConfig().getIntegerList("Time-Manager.Time-Left-Intervals")) {
      if(timers == pluginArena.getTimer()) {
        pluginArena.sendBuildLeftTimeMessage();
        break;
      }
    }
    checkPlayerOutSidePlot(pluginArena);
  }

  private void checkPlayerOutSidePlot(GuessArena pluginArena) {
    if(pluginArena.getArenaOption("IN_PLOT_CHECKER") >= 3) {
      pluginArena.setArenaOption("IN_PLOT_CHECKER", 0);
      for(Player player : pluginArena.getPlayersLeft()) {
        User user = getPlugin().getUserManager().getUser(player);
        Plot buildPlot = pluginArena.getPlotFromPlayer(player);
        player.setPlayerWeather(buildPlot.getWeatherType());
        player.setPlayerTime(Plot.Time.format(buildPlot.getTime(), player.getWorld().getTime()), false);
        if(buildPlot != null && buildPlot.getCuboid() != null && !buildPlot.getCuboid().isInWithMarge(player.getLocation(), 5)) {
          player.teleport(buildPlot.getTeleportLocation());
          new MessageBuilder("IN_GAME_MESSAGES_PLOT_PERMISSION_OUTSIDE").asKey().arena(pluginArena).player(player).sendPlayer();
        }
      }
    }
    pluginArena.changeArenaOptionBy("IN_PLOT_CHECKER", 1);
  }
}