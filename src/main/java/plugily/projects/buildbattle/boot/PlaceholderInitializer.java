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

package plugily.projects.buildbattle.boot;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import plugily.projects.buildbattle.Main;
import plugily.projects.buildbattle.arena.ArenaRegistry;
import plugily.projects.buildbattle.arena.BaseArena;
import plugily.projects.buildbattle.arena.BuildArena;
import plugily.projects.buildbattle.arena.GuessArena;
import plugily.projects.buildbattle.arena.managers.plots.Plot;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.placeholder.Placeholder;
import plugily.projects.minigamesbox.classic.handlers.placeholder.PlaceholderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.10.2022
 */
public class PlaceholderInitializer {

  private final Main plugin;

  public PlaceholderInitializer(Main plugin) {
    this.plugin = plugin;
    registerPlaceholders();
  }

  public void registerPlaceholders() {
    getPlaceholderManager().registerPlaceholder(new Placeholder("theme", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getTheme(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getTheme(arena);
      }

      @Nullable
      private String getTheme(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        String theme = pluginArena.getTheme();
        if(theme.equalsIgnoreCase("theme")) {
          theme = new MessageBuilder("SCOREBOARD_THEME_UNKNOWN").asKey().build();
        }
        return theme;
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("difficulty", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getTheme(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getTheme(arena);
      }

      @Nullable
      private String getTheme(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena instanceof GuessArena) {
          return ((GuessArena) pluginArena).getCurrentBBTheme().getDifficulty().name();
        }
        return null;
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("difficulty_pointsreward", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getTheme(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getTheme(arena);
      }

      @Nullable
      private String getTheme(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena instanceof GuessArena) {
          return Integer.toString(((GuessArena) pluginArena).getCurrentBBTheme().getDifficulty().getPointsReward());
        }
        return null;
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("builder", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getBuilder(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getBuilder(arena);
      }

      @Nullable
      private String getBuilder(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(!(pluginArena instanceof GuessArena)) {
          return null;
        }
        return ((GuessArena) pluginArena).getCurrentBuilder().getName();
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("type", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getType(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getType(arena);
      }

      @Nullable
      private String getType(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        return pluginArena.getArenaType().toString();
      }
    });
    getPlaceholderManager().registerPlaceholder(new Placeholder("type_pretty", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getType(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getType(arena);
      }

      @Nullable
      private String getType(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        return pluginArena.getArenaType().getPrefix();
      }
    });
    for(int i = 0; i <= 32; i++) {
      final int number = i;
      getPlaceholderManager().registerPlaceholder(new Placeholder("place_member_" + number, Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
        @Override
        public String getValue(Player player, PluginArena arena) {
          return getPlace(arena);
        }

        @Override
        public String getValue(PluginArena arena) {
          return getPlace(arena);
        }

        @Nullable
        private String getPlace(PluginArena arena) {
          BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
          if(pluginArena == null) {
            return null;
          }
          if(pluginArena instanceof BuildArena) {
            if(pluginArena.getArenaInGameStage() != BaseArena.ArenaInGameStage.PLOT_VOTING && pluginArena.getArenaState() != ArenaState.ENDING) {
              return null;
            }
            StringBuilder members = new StringBuilder();
            ((BuildArena) pluginArena).getTopList().get(number).forEach(p -> members.append(p.getName()).append(" & "));
            members.delete(members.length() - 2, members.length());
            return new MessageBuilder(members.toString()).build();
          }
          if(pluginArena instanceof GuessArena) {
            if(pluginArena.getArenaInGameStage() != BaseArena.ArenaInGameStage.PLOT_VOTING && pluginArena.getArenaState() != ArenaState.ENDING) {
              return null;
            }
            return new ArrayList<>(((GuessArena) pluginArena).getPlayersPoints().entrySet()).get(number).getKey().getName();
          }
          return pluginArena.getArenaInGameStage().toString();
        }
      });

      getPlaceholderManager().registerPlaceholder(new Placeholder("place_points_" + number, Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
        @Override
        public String getValue(Player player, PluginArena arena) {
          return getPlace(arena);
        }

        @Override
        public String getValue(PluginArena arena) {
          return getPlace(arena);
        }

        @Nullable
        private String getPlace(PluginArena arena) {
          BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
          if(pluginArena == null) {
            return null;
          }
          if(pluginArena instanceof BuildArena) {
            if(pluginArena.getArenaInGameStage() != BaseArena.ArenaInGameStage.PLOT_VOTING && pluginArena.getArenaState() != ArenaState.ENDING) {
              return null;
            }
            return String.valueOf(((BuildArena) pluginArena).getPlotFromPlayer(((BuildArena) pluginArena).getTopList().get(number).get(0)).getPoints());
          }
          if(pluginArena instanceof GuessArena) {
            if(pluginArena.getArenaInGameStage() != BaseArena.ArenaInGameStage.PLOT_VOTING && pluginArena.getArenaState() != ArenaState.ENDING) {
              return null;
            }
            return String.valueOf(new ArrayList<>(((GuessArena) pluginArena).getPlayersPoints().entrySet()).get(number).getValue());
          }
          return null;
        }
      });
    }
    getPlaceholderManager().registerPlaceholder(new Placeholder("ingame_stage", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getStage(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getStage(arena);
      }

      @Nullable
      private String getStage(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        if(pluginArena.getArenaInGameStage() == null) {
          return null;
        }
        return pluginArena.getArenaInGameStage().toString();
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("ingame_stage_pretty", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getStage(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getStage(arena);
      }

      @Nullable
      private String getStage(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        if(pluginArena.getArenaInGameStage() == null) {
          return null;
        }
        return pluginArena.getArenaInGameStage().getPrefix();
      }
    });


    getPlaceholderManager().registerPlaceholder(new Placeholder("team", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getMembers(player, arena);
      }

      @Nullable
      private String getMembers(Player player, PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        if(!(pluginArena instanceof BuildArena)) {
          return new MessageBuilder("IN_GAME_MESSAGES_PLOT_NOBODY").asKey().build();
        }
        Plot plot = pluginArena.getPlotManager().getPlot(player);

        if(plot != null) {
          if(plot.getMembers().size() > 1) {
            StringBuilder members = new StringBuilder();
            plot.getMembers().forEach(p -> members.append(p.getName()).append(" & "));
            members.deleteCharAt(members.length() - 2);
            return new MessageBuilder(members.toString()).build();
          }
        }
        return new MessageBuilder("IN_GAME_MESSAGES_PLOT_NOBODY").asKey().build();
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("summary_player", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getSummary(arena, player);
      }

      @Nullable
      private String getSummary(PluginArena arena, Player player) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        if(pluginArena instanceof BuildArena) {
          Plot plot = ((BuildArena) pluginArena).getWinnerPlot();
          if(plot != null && !plot.getMembers().isEmpty() && plot.getMembers().contains(player)) {
            return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WIN").asKey().arena(pluginArena).build();
          }
        }
        if(pluginArena instanceof GuessArena) {
          if(((GuessArena) pluginArena).getWinner() == player) {
            return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WIN").asKey().arena(pluginArena).build();
          }
        }
        return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_LOSE").asKey().arena(pluginArena).build();
      }
    });

    getPlaceholderManager().registerPlaceholder(new Placeholder("summary_place_own", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getSummary(arena, player);
      }

      @Nullable
      private String getSummary(PluginArena arena, Player player) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        if(pluginArena instanceof BuildArena) {
          if(pluginArena.getArenaInGameStage() != BaseArena.ArenaInGameStage.PLOT_VOTING && pluginArena.getArenaState() != ArenaState.ENDING) {
            return null;
          }
          List<List<Player>> playerList = new ArrayList<>(((BuildArena) pluginArena).getTopList().values());
          for(int playerPlace = 0; playerPlace < playerList.size(); playerPlace++) {
            if(playerList.get(playerPlace).contains(player)) {
              return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_OWN").asKey().integer(playerPlace + 1).arena(pluginArena).build();
            }
          }
        }
        if(pluginArena instanceof GuessArena) {
          if(pluginArena.getArenaInGameStage() != BaseArena.ArenaInGameStage.PLOT_VOTING && pluginArena.getArenaState() != ArenaState.ENDING) {
            return null;
          }
          int playerPlace = new ArrayList<>(((GuessArena) pluginArena).getPlayersPoints().keySet()).indexOf(player);
          if(playerPlace != -1) {
            return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_OWN").asKey().integer(playerPlace).arena(pluginArena).build();
          }
        }
        return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_OWN").asKey().integer(0).arena(pluginArena).build();
      }
    });


    getPlaceholderManager().registerPlaceholder(new Placeholder("summary", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getSummary(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getSummary(arena);
      }

      @Nullable
      private String getSummary(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        if(pluginArena instanceof BuildArena) {
          Plot plot = ((BuildArena) pluginArena).getWinnerPlot();
          if(plot != null && !plot.getMembers().isEmpty()) {
            return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WINNER").asKey().arena(pluginArena).build();
          }
        }
        if(pluginArena instanceof GuessArena) {
          if(((GuessArena) pluginArena).getWinner() != null) {
            return new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_WINNER").asKey().arena(pluginArena).build();
          }
        }
        return null;
      }
    });
    getPlaceholderManager().registerPlaceholder(new Placeholder("summary_place_list", Placeholder.PlaceholderType.ARENA, Placeholder.PlaceholderExecutor.ALL) {
      @Override
      public String getValue(Player player, PluginArena arena) {
        return getSummary(arena);
      }

      @Override
      public String getValue(PluginArena arena) {
        return getSummary(arena);
      }

      @Nullable
      private String getSummary(PluginArena arena) {
        BaseArena pluginArena = getArenaRegistry().getArena(arena.getId());
        if(pluginArena == null) {
          return null;
        }
        if(pluginArena.getArenaInGameStage() != BaseArena.ArenaInGameStage.PLOT_VOTING && pluginArena.getArenaState() != ArenaState.ENDING) {
          return null;
        }
        int places = pluginArena.getPlayersLeft().size();
        if(places > 16) {
          places = 16;
        }
        StringBuilder placeSummary = new StringBuilder();
        for(int i = 1; i < places; i++) {
          //number = place, value = members + points
          placeSummary.append("\n").append(new MessageBuilder("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_PLACE").asKey().integer(i).value("%arena_place_member_" + i + "% (%arena_place_points_" + i + "%)").arena(pluginArena).build());
        }
        return placeSummary.toString();
      }
    });
  }

  private PlaceholderManager getPlaceholderManager() {
    return plugin.getPlaceholderManager();
  }

  private ArenaRegistry getArenaRegistry() {
    return plugin.getArenaRegistry();
  }

}
