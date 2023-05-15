package org.opencommunity.goodtrade.inventories;

import java.util.Optional;
import org.opencommunity.goodtrade.GoodTrade;
import org.opencommunity.goodtrade.Shop;
import org.opencommunity.goodtrade.RowStore;
import org.opencommunity.goodtrade.Utils;
import org.opencommunity.goodtrade.gui.GUI;
import org.opencommunity.goodtrade.utils.FormatUtil;
import org.opencommunity.goodtrade.utils.LocaleAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class InvShop extends GUI {
	public static boolean listAllShops = GoodTrade.config.getBoolean("publicShopListCommand");

	private static String getShopName(Shop shop, Player player) {
		String shopId = String.valueOf(shop.shopId());
		if(shop.isAdmin())
			return LocaleAPI.getMessage(player, "admin_shop").replaceAll("%id", shopId);
		String msg = LocaleAPI.getMessage(player, "normal_shop");
		OfflinePlayer pl = Bukkit.getOfflinePlayer(shop.getOwner());
		if(pl == null)
			return msg.replaceAll("%player%", "<unknown>");
		return msg.replaceAll("%player%", pl.getName()).replaceAll("%id", shopId);
	}

	public InvShop(Shop shop, Player player) {
		super(54, getShopName(shop, player));
		for(int x=0; x<9; x++) {
			for(int y=0; y<6; y++) {
				if(x == 1) {
					if(y == 0)
						placeItem(y*9+x, GUI.createItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + LocaleAPI.getMessage(player, "sell_title")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemOut());
					}
				} else if(x == 2) {
					if(y == 0)
						placeItem(y*9+x, GUI.createItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + LocaleAPI.getMessage(player, "sell_title_2")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemOut2());
					}
				} else if(x == 5) {
					if(y == 0)
						placeItem(x, GUI.createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + LocaleAPI.getMessage(player, "buy_title")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemIn());
					}
				} else if(x == 6) {
					if(y == 0)
						placeItem(y*9+x, GUI.createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED + LocaleAPI.getMessage(player, "buy_title_2")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemIn2());
					}
				} else if(x == 8 && y == 0) {
					if(listAllShops) {
						placeItem(y*9+x, GUI.createItem(Material.END_CRYSTAL, LocaleAPI.getMessage(player, "shop_list_title")), p -> {
							p.closeInventory();
							p.performCommand("shop shops");
						});
					} else
						placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
				} else if(x == 8 && y >= 1) {
					Optional<RowStore> row = shop.getRow(y-1);
					if(row.isPresent()) {
						final int index = y - 1;
						if(row.get().getItemOut().isSimilar(row.get().getItemOut2()) && !Utils.hasDoubleItemStock(shop, row.get().getItemOut(), row.get().getItemOut2()))
							placeItem(y * 9 + x, GUI.createItem(Material.RED_DYE, FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "no_stock_button"))), p -> {
								p.closeInventory();
								shop.buy(p, index);
							});
						else if(!Utils.hasStock(shop, row.get().getItemOut()))
							placeItem(y * 9 + x, GUI.createItem(Material.RED_DYE, FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "no_stock_button"))), p -> {
								p.closeInventory();
								shop.buy(p, index);
							});
						else if(!Utils.hasStock(shop, row.get().getItemOut2()))
							placeItem(y * 9 + x, GUI.createItem(Material.RED_DYE, FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "no_stock_button"))), p -> {
								p.closeInventory();
								shop.buy(p, index);
							});
						else {
							placeItem(y * 9 + x, GUI.createItem(Material.LIME_DYE, ChatColor.BOLD +LocaleAPI.getMessage(player, "buy_action")), p -> {
								p.closeInventory();
								shop.buy(p, index);
							});
						}
					} else
						placeItem(y*9+x, GUI.createItem(Material.GRAY_DYE, ""));
				} else
					placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
			}
		}
	}
}
