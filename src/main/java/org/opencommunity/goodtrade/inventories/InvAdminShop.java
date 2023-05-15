package org.opencommunity.goodtrade.inventories;

import java.util.Optional;
import org.opencommunity.goodtrade.GoodTrade;
import org.opencommunity.goodtrade.Permission;
import org.opencommunity.goodtrade.RowStore;
import org.opencommunity.goodtrade.Shop;
import org.opencommunity.goodtrade.Utils;
import org.opencommunity.goodtrade.utils.FormatUtil;
import org.opencommunity.goodtrade.utils.LocaleAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.opencommunity.goodtrade.gui.GUI;

public class InvAdminShop extends GUI {
	public static boolean remoteManage = GoodTrade.config.getBoolean("remoteManage");
	public static boolean stockGUIShop = GoodTrade.config.getBoolean("enableStockAccessFromShopGUI");
	public static boolean stockCommandEnabled = GoodTrade.config.getBoolean("enableStockCommand");
	public static boolean usePerms = GoodTrade.config.getBoolean("usePermissions");
	public static int maxPages = GoodTrade.config.getInt("stockPages");
	public static int permissionMax = GoodTrade.config.getInt("maxStockPages");
	private final Shop shop;

	public InvAdminShop(Shop shop, Player player) {
		super(54, getShopName(shop, player));
		this.shop = shop;
		updateItems(player);
	}

	private static String getShopName(Shop shop, Player player) {
		String shopId = String.valueOf(shop.shopId());
		if(shop.isAdmin())
			return LocaleAPI.getMessage(player, "admin_shop").replaceAll("%id", shopId);
		String msg = LocaleAPI.getMessage(player, "normal_shop").replaceAll("%id", shopId);
		OfflinePlayer pl = Bukkit.getOfflinePlayer(shop.getOwner());
		if(pl == null)
			return msg.replaceAll("%player%", "<unknown>");
		return msg.replaceAll("%player%", pl.getName()).replaceAll("%id", shopId);
	}

	private void updateItems(Player player) {
		for(int x=0; x<9; x++) {
			for(int y=0; y<6; y++) {
				if(x == 1) {
					if(y == 0)
						placeItem(y*9+x, GUI.createItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN+ LocaleAPI.getMessage(player, "sell_title")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemOut());
					}
				} else if(x == 2) {
					if(y == 0)
						placeItem(y*9+x, GUI.createItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN+ LocaleAPI.getMessage(player, "sell_title_2")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemOut2());
					}
				} else if(x == 4) {
					if(y == 0)
						placeItem(y*9+x, GUI.createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED+ LocaleAPI.getMessage(player, "buy_title")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemIn());
					}
				} else if(x == 5) {
					if(y == 0)
						placeItem(y*9+x, GUI.createItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED+ LocaleAPI.getMessage(player, "buy_title_2")));
					else {
						Optional<RowStore> row = shop.getRow(y-1);
						if(row.isPresent())
							placeItem(y*9+x, row.get().getItemIn2());
					}
				} else if(x == 7 && y > 0) {
					Optional<RowStore> row = shop.getRow(y-1);
					final int index = y-1;
					if(row.isPresent()) {
						placeItem(y*9+x, GUI.createItem(Material.TNT, ChatColor.BOLD+ LocaleAPI.getMessage(player, "delete_title")), p -> {
							shop.delete(index);
							InvAdminShop inv = new InvAdminShop(shop, p.getPlayer());
							inv.open(p);
						});
					} else {
						placeItem(y*9+x, GUI.createItem(Material.LIME_DYE, ChatColor.BOLD+ LocaleAPI.getMessage(player, "create_title")), p -> {
							InvCreateRow inv = new InvCreateRow(shop, index, player);
							inv.open(p);
						});
					}
				} else if(x == 7 && y == 0) {
					if(stockGUIShop && !shop.isAdmin() && shop.getOwner().equals(player.getUniqueId())) {
						placeItem(y * 9 + x, GUI.createItem(Material.CHEST, LocaleAPI.getMessage(player, "stock_title")), p -> {
							p.closeInventory();
							InvStock inv = InvStock.getInvStock(player.getUniqueId(), player);
							int maxStockPages = maxPages;
							if(usePerms) {
								String permPrefix = Permission.SHOP_STOCK_PREFIX.toString();
								int maxPermPages = InvAdminShop.permissionMax;
								boolean permissionFound = false;
								for(int i=maxPermPages; i>0; i--)
									if(player.hasPermission(permPrefix + i)) {
										maxStockPages = i;
										permissionFound = true;
										break;
									}
								if(!permissionFound)
									maxStockPages = maxPermPages;
							}
							inv.setMaxPages(maxStockPages);
							inv.setPag(0);
							InvStock.inShopInv.put(player, player.getUniqueId());
							inv.open(player);
						});
					} else
						placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
				} else if(x == 8 && y == 0) {
					if(InvShop.listAllShops && Shop.showOwnedShops && remoteManage) {
						placeItem(y*9+x, GUI.createItem(Material.END_CRYSTAL, LocaleAPI.getMessage(player, "shop_list_title")), p -> {
							p.closeInventory();
							p.performCommand("shop shops");
						});
					} else
						placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
				} else if(x == 8 && y >= 1) {
					final Optional<RowStore> row = shop.getRow(y-1);
					if(shop.isAdmin()) {
						if(row.isPresent()) {
							if(row.get().broadcast) {
								placeItem(y * 9 + x, GUI.createItem(Material.REDSTONE_TORCH, LocaleAPI.getMessage(player, "broadcast_on"), (short) 15), p -> {
									row.get().toggleBroadcast();
									updateItems(player);
								});
							} else {
								placeItem(y * 9 + x, GUI.createItem(Material.LEVER, LocaleAPI.getMessage(player, "broadcast_off"), (short) 15), p -> {
									row.get().toggleBroadcast();
									updateItems(player);
								});
							}
						} else
							placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
					} else {
						if(row.isPresent()) {
							if(row.get().getItemOut().isSimilar(row.get().getItemOut2()) && !Utils.hasDoubleItemStock(shop, row.get().getItemOut(), row.get().getItemOut2()))
								placeItem(y*9+x, GUI.createItem(Material.RED_DYE, FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "no_stock_button"))));
							else if(!Utils.hasStock(shop, row.get().getItemOut()))
								placeItem(y*9+x, GUI.createItem(Material.RED_DYE, FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "no_stock_button"))));
							else if(!Utils.hasStock(shop, row.get().getItemOut2()))
								placeItem(y*9+x, GUI.createItem(Material.RED_DYE, FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "no_stock_button"))));
							else
								placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
						} else
							placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
					}
				} else
					placeItem(y*9+x, GUI.createItem(Material.BLACK_STAINED_GLASS_PANE, ""));
			}
		}
	}
}
