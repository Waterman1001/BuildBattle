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

package plugily.projects.buildbattle.handlers.menu.registry.banner;

import org.bukkit.DyeColor;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import plugily.projects.buildbattle.Main;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.ItemBuilder;
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.inventory.common.item.SimpleClickableItem;
import plugily.projects.minigamesbox.inventory.normal.NormalFastInv;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Plajer
 * <p>
 * Created at 16.07.2019
 */
public class BannerMenu {

  private static Main plugin;
  private final Map<PatternStage, NormalFastInv> guiStages = new EnumMap<>(PatternStage.class);
  private final Banner banner;
  private final Player player;

  public BannerMenu(Player player) {
    this(player, new Banner());
  }

  public BannerMenu(Player player, Banner banner) {
    this.player = player;
    this.banner = banner;
    prepareBaseStageGui();
    prepareLayerStageGui();
    prepareLayerColorStageGui();
  }

  public static void init(Main plugin) {
    BannerMenu.plugin = plugin;
  }

  @SuppressWarnings("deprecation")
  private void prepareBaseStageGui() {
    NormalFastInv gui = new NormalFastInv(6, new MessageBuilder("MENU_OPTION_CONTENT_BANNER_INVENTORY_COLOR").asKey().build());
    for(DyeColor color : DyeColor.values()) {
      ItemStack item;
      if(ServerVersion.Version.isCurrentEqualOrLower(ServerVersion.Version.v1_12_R1)) {
        item = XMaterial.WHITE_BANNER.parseItem();
        BannerMeta meta = (BannerMeta) item.getItemMeta();
        if(ServerVersion.Version.isCurrentEqualOrLower(ServerVersion.Version.v1_12_R1)) {
          meta.setBaseColor(color);
        } else {
          ((org.bukkit.block.Banner) item).setBaseColor(color);
        }
        item.setItemMeta(meta);
      } else {
        item = XMaterial.matchXMaterial(color.toString().toUpperCase() + "_BANNER").get().parseItem();
      }
      gui.addItem(new SimpleClickableItem(item, e -> {
        e.setCancelled(true);
        banner.setBaseColor(color);
        new BannerMenu(player, banner).openInventory(PatternStage.LAYER);
      }));
    }
    addCreatorItem(gui);
    addGoBackItem(gui);
    guiStages.put(PatternStage.BASE, gui);
  }

  private void prepareLayerStageGui() {
    NormalFastInv gui = new NormalFastInv(6, new MessageBuilder("MENU_OPTION_CONTENT_BANNER_INVENTORY_LAYER").asKey().build());
    for(PatternType pattern : PatternType.values()) {
      ItemStack item = banner.buildBanner();
      BannerMeta meta = (BannerMeta) item.getItemMeta();
      DyeColor color = banner.getColor() == DyeColor.BLACK ? DyeColor.WHITE : DyeColor.BLACK;
      meta.addPattern(new Pattern(color, pattern));
      item.setItemMeta(meta);
      gui.addItem(new SimpleClickableItem(item, e -> {
        e.setCancelled(true);
        banner.addPattern(new BannerPattern(color, pattern));
        new BannerMenu(player, banner).openInventory(PatternStage.LAYER_COLOR);
      }));
    }
    addCreatorItem(gui);
    addGoBackItem(gui);
    guiStages.put(PatternStage.LAYER, gui);
  }

  private void prepareLayerColorStageGui() {
    NormalFastInv gui = new NormalFastInv(6, new MessageBuilder("MENU_OPTION_CONTENT_BANNER_INVENTORY_LAYER_COLOR").asKey().build());
    for(DyeColor color : DyeColor.values()) {
      ItemStack item = banner.buildBanner();
      BannerMeta meta = (BannerMeta) item.getItemMeta();
      Pattern pattern = new Pattern(color, banner.getLastPattern().getPatternType());
      meta.addPattern(pattern);
      item.setItemMeta(meta);
      gui.addItem(new SimpleClickableItem(item, e -> {
        e.setCancelled(true);
        banner.replaceLastPattern(new BannerPattern(color, banner.getLastPattern().getPatternType()));
        new BannerMenu(player, banner).openInventory(PatternStage.LAYER);
      }));
    }
    addCreatorItem(gui);
    addGoBackItem(gui);
    guiStages.put(PatternStage.LAYER_COLOR, gui);
  }

  private void addCreatorItem(NormalFastInv gui) {
    gui.setItem(49, new SimpleClickableItem(new ItemBuilder(banner.buildBanner())
        .name(new MessageBuilder("MENU_OPTION_CONTENT_BANNER_ITEM_CREATE_NAME").asKey().build())
        .lore(new MessageBuilder("MENU_OPTION_CONTENT_BANNER_ITEM_CREATE_LORE").asKey().build())
        .build(), e -> {
      e.setCancelled(true);
      e.getWhoClicked().closeInventory();
      player.getInventory().addItem(banner.buildBanner());
    }));
  }

  private void addGoBackItem(NormalFastInv gui) {
    gui.setItem(45, new SimpleClickableItem(plugin.getOptionsRegistry().getGoBackItem(), e -> {
      e.setCancelled(true);
      e.getWhoClicked().closeInventory();
      player.openInventory(plugin.getOptionsRegistry().formatInventory());
    }));
  }

  public void openInventory(PatternStage stage) {
    guiStages.get(stage).open(player);
  }

  public enum PatternStage {
    BASE, LAYER, LAYER_COLOR
  }

}