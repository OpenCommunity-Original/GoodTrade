package org.opencommunity.goodtrade;

import org.opencommunity.goodtrade.inventories.*;
import org.opencommunity.goodtrade.utils.FormatUtil;
import org.opencommunity.goodtrade.utils.LocaleAPI;
import org.opencommunity.goodtrade.utils.Util;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.util.*;

public class CommandShop implements CommandExecutor {
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if(sender instanceof ConsoleCommandSender && args.length > 0) {
			if(args[0].equalsIgnoreCase("reload")) {
				reloadShop(null);
				return true;
			}
			else if(args[0].equalsIgnoreCase("deleteid") && args[1] != null) {
				deleteShopID(null, args[1]);
				return true;
			}
			else if(args[0].equalsIgnoreCase("createlocation") && args.length > 5) {
				Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> createLocation(null, args[1], args[2], args[3], args[4], args[5]));
				return true;
			}
			else if(args[0].equalsIgnoreCase("deletelocation") && args.length > 4) {
				Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> deleteLocation(null, args[1], args[2], args[3], args[4]));
				return true;
			}
			else {
				sender.sendMessage(ChatColor.GOLD + "GoodTrade Console Commands:");
				sender.sendMessage(ChatColor.GRAY + label + " createlocation <player> <x> <y> <z> <world>");
				sender.sendMessage(ChatColor.GRAY + label + " deletelocation <x> <y> <z> <world>");
				sender.sendMessage(ChatColor.GRAY + label + " deleteid <id>");
				sender.sendMessage(ChatColor.GRAY + label + " reload");
				return false;
			}
		}
		if(!(sender instanceof Player)) {
			if(sender instanceof ConsoleCommandSender) {
				sender.sendMessage(ChatColor.GOLD + "GoodTrade Console Commands:");
				sender.sendMessage(ChatColor.GRAY + label + " createlocation <player> <x> <y> <z> <world>");
				sender.sendMessage(ChatColor.GRAY + label + " deletelocation <x> <y> <z> <world>");
				sender.sendMessage(ChatColor.GRAY + label + " deleteid <id>");
				sender.sendMessage(ChatColor.GRAY + label + " reload");
			}
			else
				Util.sendMessage((Player) sender, "not_player");
			return false;
		}
		Player player = (Player) sender;
		if(args.length == 0)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> listSubCmd(player, label));
		else if(args[0].equalsIgnoreCase("adminshop"))
			adminShop(player);
		else if(args[0].equalsIgnoreCase("count") && args.length >= 2)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> count(player, args[1]));
		else if(args[0].equalsIgnoreCase("create"))
			createStore(player);
		else if(args[0].equalsIgnoreCase("createshop") && args.length >= 2)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> createShop(player, args[1]));
		else if(args[0].equalsIgnoreCase("createlocation") && args.length > 5)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> createLocation(player, args[1], args[2], args[3], args[4], args[5]));
		else if(args[0].equalsIgnoreCase("delete"))
			deleteShop(player);
		else if(args[0].equalsIgnoreCase("deleteid") && args.length >= 2)
			deleteShopID(player, args[1]);
		else if(args[0].equalsIgnoreCase("deletelocation") && args.length > 4)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> deleteLocation(player, args[1], args[2], args[3], args[4]));
		else if(args[0].equalsIgnoreCase("list") && args.length == 1)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> listShops(player, null));
		else if(args[0].equalsIgnoreCase("list") && args.length >= 2)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> listShops(player, args[1]));
		else if(args[0].equalsIgnoreCase("listadmin"))
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> listAdminShops(player));
		else if(args[0].equalsIgnoreCase("manage") && args.length >= 2)
			shopManage(player, args[1]);
		else if(args[0].equalsIgnoreCase("managestock") && args.length == 2)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> manageStock(player, args[1], "1"));
		else if(args[0].equalsIgnoreCase("managestock") && args.length >= 3)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> manageStock(player, args[1], args[2]));
		else if(args[0].equalsIgnoreCase("out") && args.length == 1)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> outOfStock(player, null));
		else if(args[0].equalsIgnoreCase("out") && args.length >= 2)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> outOfStock(player, args[1]));
		else if(args[0].equalsIgnoreCase("reload"))
			reloadShop(player);
		else if(args[0].equalsIgnoreCase("removeallshops") && args.length >= 2)
			removeAllShops(player, args[1]);
		else if(args[0].equalsIgnoreCase("shops"))
			listAllShops(player);
		else if(args[0].equalsIgnoreCase("sold") && args.length == 1)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> shopSold(player, null));
		else if(args[0].equalsIgnoreCase("sold") && args.length >= 2)
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> shopSold(player, args[1]));
		else if(args[0].equalsIgnoreCase("stock") && args.length == 1)
			stockShop(player, "1");
		else if(args[0].equalsIgnoreCase("stock") && args.length >= 2)
			stockShop(player, args[1]);
		else if(args[0].equalsIgnoreCase("view") && args.length >= 2)
			viewShop(player, args[1]);
		else
			Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> listSubCmd(player, label));

		return true;
	}

	private void listSubCmd(Player player, String label) {
		player.sendMessage(ChatColor.GOLD + "GoodTrade Commands:");
		player.sendMessage(ChatColor.GRAY + "/" + label + " count <item>");
		player.sendMessage(ChatColor.GRAY + "/" + label + " create");
		player.sendMessage(ChatColor.GRAY + "/" + label + " delete");
		player.sendMessage(ChatColor.GRAY + "/" + label + " deleteid <id>");
		player.sendMessage(ChatColor.GRAY + "/" + label + " list");
		if(GoodTrade.config.getBoolean("publicListCommand") || player.hasPermission(Permission.SHOP_ADMIN.toString()) || player.hasPermission(Permission.SHOP_LIST.toString()))
			player.sendMessage(ChatColor.GRAY + "/" + label + " list <player>");
		if(GoodTrade.config.getBoolean("publicShopListCommand") || player.hasPermission(Permission.SHOP_ADMIN.toString()) || player.hasPermission(Permission.SHOP_SHOPS.toString()))
			player.sendMessage(ChatColor.GRAY + "/" + label + " shops");
		player.sendMessage(ChatColor.GRAY + "/" + label + " manage <id>");
		player.sendMessage(ChatColor.GRAY + "/" + label + " out");
		if(player.hasPermission(Permission.SHOP_ADMIN.toString()))
			player.sendMessage(ChatColor.GRAY + "/" + label + " out <player>");
		if(GoodTrade.config.getBoolean("enableShopSoldMessage"));
		player.sendMessage(ChatColor.GRAY + "/" + label + " sold <page/clear>");
		player.sendMessage(ChatColor.GRAY + "/" + label + " stock <page>");
		player.sendMessage(ChatColor.GRAY + "/" + label + " view <id>");
		if(player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			player.sendMessage(ChatColor.GRAY + "/" + label + " adminshop");
			player.sendMessage(ChatColor.GRAY + "/" + label + " createshop <player>");
			player.sendMessage(ChatColor.GRAY + "/" + label + " createlocation <player> <x> <y> <z> <world>");
			player.sendMessage(ChatColor.GRAY + "/" + label + " deletelocation <x> <y> <z> <world>");
			player.sendMessage(ChatColor.GRAY + "/" + label + " listadmin");
			player.sendMessage(ChatColor.GRAY + "/" + label + " managestock <player> <page>");
			player.sendMessage(ChatColor.GRAY + "/" + label + " reload");
			player.sendMessage(ChatColor.GRAY + "/" + label + " removeallshops <player>");
		}
	}

	private void count(Player player, String itemName) {
		if(Shop.getNumShops(player.getUniqueId()) < 1 && EventShop.noShopNoStock) {
			Util.sendMessage(player, "no_shop_stock");
			return;
		}
		Material material = Material.matchMaterial(itemName);
		if(material == null) {
			try {
				material = Material.matchMaterial(itemName.split("minecraft:")[1].toUpperCase());
			} catch(Exception ignored) { }
			if(material == null) {
				Util.sendMessage(player, "count_error");
				return;
			}
		}
		ItemStack item = new ItemStack(material);
		int max;
		if(InvAdminShop.usePerms)
			max = InvAdminShop.permissionMax;
		else
			max = InvAdminShop.maxPages;
		int itemAmountCount = 0;
		for(int i=0; i<max; i++) {
			Optional<StockShop> stockStore = StockShop.getStockShopByOwner(player.getUniqueId(),i);
			if(!stockStore.isPresent())
				continue;
			if(stockStore.get().getInventory().contains(item.getType()))
				for(int j=0; j<stockStore.get().getInventory().getSize()-1; j++)
					if(stockStore.get().getInventory().getItem(j) != null && stockStore.get().getInventory().getItem(j).getType().equals(item.getType()))
						itemAmountCount += stockStore.get().getInventory().getItem(j).getAmount();
		}
		if(itemAmountCount>0)
			player.sendMessage(FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "count_amount")).replaceAll("%amount", String.valueOf(itemAmountCount)).replaceAll("%item", itemName));
		else
			player.sendMessage(FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "count_empty")).replaceAll("%item", itemName));
	}

	private void createStore(Player player) {
		if(GoodTrade.config.getBoolean("usePermissions") && !player.hasPermission(Permission.SHOP_CREATE.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		if(!GoodTrade.config.getBoolean("enableShopBlock")) {
			Util.sendMessage(player, "disabled_shop_block");
			return;
		}
		Block block = player.getTargetBlockExact(5);
		if(block == null) {
			Util.sendMessage(player, "target_mismatch");
			return;
		}
		if(GoodTrade.config.getBoolean("disableShopInWorld")) {
			List<String> disabledWorldsList = GoodTrade.config.getStringList("disabledWorldList");
			for(String disabledWorlds:disabledWorldsList) {
				if(disabledWorlds != null && block.getWorld().getName().equals(disabledWorlds)) {
					Util.sendMessage(player, "disabled_world");
					return;
				}
			}
		}
		String shopBlock = GoodTrade.config.getString("shopBlock");
		Material match = Material.matchMaterial(shopBlock);
		if(match == null) {
			try {
				match = Material.matchMaterial(shopBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception ignored) { }
			if(match == null)
				match = Material.BARREL;
		}
		if(!EventShop.multipleShopBlocks) {
			if(!block.getType().equals(match)) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		} else {
			boolean shopMatch = false;
			for(String shopBlocks:EventShop.multipleShopBlock) {
				Material shopListBlocks = Material.matchMaterial(shopBlocks);
				if(shopListBlocks != null && block.getType().equals(shopListBlocks)) {
					shopMatch = true;
					break;
				}
			}
			if(!block.getType().equals(match) && !shopMatch) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		}
		
		Optional<Shop> shop = Shop.getShopByLocation(block.getLocation());
		if(shop.isPresent()) {
			Util.sendMessage(player, "existing_shop");
			return;
		}
		boolean limitShops;
		int numShops = Shop.getNumShops(player.getUniqueId());
		if(GoodTrade.config.getBoolean("usePermissions")) {
			int maxShops = 0;
			String permPrefix = Permission.SHOP_LIMIT_PREFIX.toString();
			for(PermissionAttachmentInfo attInfo : player.getEffectivePermissions()) {
				String perm = attInfo.getPermission();
				if(perm.startsWith(permPrefix)) {
					int num;
					try {
						num = Integer.parseInt(perm.substring(perm.lastIndexOf(".")+1));
					} catch(Exception e) { num = 0; }
					if(num > maxShops)
						maxShops = num;
				}
			}
			limitShops = numShops >= maxShops;
		}
		else {
			int numConfig = GoodTrade.config.getInt("defaultShopLimit");
			limitShops = numShops >= numConfig && numConfig >= 0;
		}
		if(player.hasPermission(Permission.SHOP_LIMIT_BYPASS.toString()))
			limitShops = false;
		if(limitShops) {
			Util.sendMessage(player, "shop_limit");
			return;
		}
		double cost = GoodTrade.config.getDouble("createCost");
		Optional<Economy> economy = GoodTrade.getEconomy();
		if(cost > 0 && economy.isPresent()) {
			OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
			EconomyResponse res = economy.get().withdrawPlayer(offPlayer, cost);
			if(!res.transactionSuccess()) {
				Util.sendMessage(player, "no_money");
				return;
			}
		}
		Shop newShop = Shop.createShop(block.getLocation(), player.getUniqueId());
		Util.sendMessage(player, "shop_created");
		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(GoodTrade.getPlugin(), () -> {
			Optional<Shop> shops = Shop.getShopByLocation(block.getLocation());
			Shop.shopList.put(shops.get().shopId(), player.getUniqueId());
		}, 10);
		InvAdminShop inv = new InvAdminShop(newShop, player);
		inv.open(player, newShop.getOwner());
	}

	private void createShop(Player player, String playerShop) {
		if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		if(!GoodTrade.config.getBoolean("enableShopBlock")) {
			Util.sendMessage(player, "disabled_shop_block");
			return;
		}
		Block block = player.getTargetBlockExact(5);
		if(block == null) {
			Util.sendMessage(player, "target_mismatch");
			return;
		}
		if(GoodTrade.config.getBoolean("disableShopInWorld")) {
			List<String> disabledWorldsList = GoodTrade.config.getStringList("disabledWorldList");
			for(String disabledWorlds:disabledWorldsList) {
				if(disabledWorlds != null && block.getWorld().getName().equals(disabledWorlds)) {
					Util.sendMessage(player, "disabled_world");
					return;
				}
			}
		}
		String shopBlock = GoodTrade.config.getString("shopBlock");
		Material match = Material.matchMaterial(shopBlock);
		if(match == null) {
			try {
				match = Material.matchMaterial(shopBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception ignored) { }
			if(match == null)
				match = Material.BARREL;
		}
		if(!EventShop.multipleShopBlocks) {
			if(!block.getType().equals(match)) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		} else {
			boolean shopMatch = false;
			for(String shopBlocks:EventShop.multipleShopBlock) {
				Material shopListBlocks = Material.matchMaterial(shopBlocks);
				if(shopListBlocks != null && block.getType().equals(shopListBlocks)) {
					shopMatch = true;
					break;
				}
			}
			if(!block.getType().equals(match) && !shopMatch) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		}
		UUID shopOwner;
		if(playerShop == null) {
			Util.sendMessage(player, "no_player_found");
			return;
		} else {
			Player playerInGame = Bukkit.getPlayer(playerShop);
			if(playerInGame != null && playerInGame.isOnline())
				shopOwner = playerInGame.getUniqueId();
			else {
				try {
					shopOwner = getUUID(playerShop);
				} catch (Exception e) {
					UUID foundPlayerUUID = null;
					boolean foundPlayer = false;
					for(OfflinePlayer offlinePlayers : Bukkit.getOfflinePlayers())
						if(offlinePlayers.getName().equalsIgnoreCase(playerShop)) {
							foundPlayerUUID = offlinePlayers.getUniqueId();
							foundPlayer = true;
							break;
						}
					if(!foundPlayer) {
						Util.sendMessage(player, "no_player_found");
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[GoodTrade] Player cannot be found!");
						return;
					}
					shopOwner = foundPlayerUUID;
				}
			}
		}
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(shopOwner);
		if(offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
			Util.sendMessage(player, "no_player_found");
			return;
		}
		
		Optional<Shop> shop = Shop.getShopByLocation(block.getLocation());
		if(!shop.isPresent()) {
			Shop.createShop(block.getLocation(), shopOwner);
			player.sendMessage(FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "player_shop_created").replaceAll("%p", playerShop)));
			final UUID shopOwnerFinal = shopOwner;
			Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(GoodTrade.getPlugin(), () -> {
				Optional<Shop> shops = Shop.getShopByLocation(block.getLocation());
				Shop.shopList.put(shops.get().shopId(), shopOwnerFinal);
			}, 10);
		} else { Util.sendMessage(player, "existing_shop"); }
	}

	private void createLocation(Player player, String playerShop, String x, String y, String z, String worldString) {
		if(player != null && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		if(!GoodTrade.config.getBoolean("enableShopBlock")) {
			Util.sendMessage(player, "disabled_shop_block");
			return;
		}
		String world;
		if(worldString != null)
			world = worldString;
		else
			world = "world";

		int xLoc,yLoc,zLoc;
		try {
			xLoc = Integer.parseInt(x);
			yLoc = Integer.parseInt(y);
			zLoc = Integer.parseInt(z);
		}
		catch(Exception e) {
			Util.sendMessage(player, "shop_location_error_num");
			return;
		}
		World worldLoc = Bukkit.getWorld(world);
		Location blockLoc;
		if(worldLoc != null)
			blockLoc = new Location(worldLoc, xLoc, yLoc, zLoc);
		else {
			Util.sendMessage(player, "shop_location_error_world");
			return;
		}
		Block block = Bukkit.getWorld(world).getBlockAt(blockLoc);
		if(GoodTrade.config.getBoolean("disableShopInWorld")) {
			List<String> disabledWorldsList = GoodTrade.config.getStringList("disabledWorldList");
			for(String disabledWorlds:disabledWorldsList) {
				if(disabledWorlds != null && block.getWorld().getName().equals(disabledWorlds)) {
					Util.sendMessage(player, "disabled_world");
					return;
				}
			}
		}
		String shopBlock = GoodTrade.config.getString("shopBlock");
		Material match = Material.matchMaterial(shopBlock);
		if(match == null) {
			try {
				match = Material.matchMaterial(shopBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception ignored) { }
			if(match == null)
				match = Material.BARREL;
		}
		if(!EventShop.multipleShopBlocks) {
			if(!block.getType().equals(match)) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		} else {
			boolean shopMatch = false;
			for(String shopBlocks:EventShop.multipleShopBlock) {
				Material shopListBlocks = Material.matchMaterial(shopBlocks);
				if(shopListBlocks != null && block.getType().equals(shopListBlocks)) {
					shopMatch = true;
					break;
				}
			}
			if(!block.getType().equals(match) && !shopMatch) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		}
		UUID shopOwner;
		if(playerShop == null) {
			Util.sendMessage(player, "no_player_found");
			return;
		} else {
			Player playerInGame = Bukkit.getPlayer(playerShop);
			if(playerInGame != null && playerInGame.isOnline())
				shopOwner = playerInGame.getUniqueId();
			else {
				try {
					shopOwner = getUUID(playerShop);
				} catch (Exception e) {
					UUID foundPlayerUUID = null;
					boolean foundPlayer = false;
					for(OfflinePlayer offlinePlayers : Bukkit.getOfflinePlayers())
						if(offlinePlayers.getName().equalsIgnoreCase(playerShop)) {
							foundPlayerUUID = offlinePlayers.getUniqueId();
							foundPlayer = true;
							break;
						}
					if(!foundPlayer) {
						if(player != null) {
							Util.sendMessage(player, "no_player_found");
						}
						else
							Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[GoodTrade] Player cannot be found!");
						return;
					}
					shopOwner = foundPlayerUUID;
				}
			}
		}
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(shopOwner);
		if(offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
			Util.sendMessage(player, "no_player_found");
			return;
		}
		Optional<Shop> shop = Shop.getShopByLocation(block.getLocation());
		if(!shop.isPresent()) {
			Shop.createShop(block.getLocation(), shopOwner);
			Map<String, String> placeholders = new HashMap<>() {
				{
					this.put("p", playerShop);
					this.put("w", world);
					this.put("x", x);
					this.put("y", y);
					this.put("z", z);
				}
			};
			Util.sendFormattedMessage(player, "shop_created_loc", placeholders);
			final UUID shopOwnerFinal = shopOwner;
			Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(GoodTrade.getPlugin(), () -> {
				Optional<Shop> shops = Shop.getShopByLocation(block.getLocation());
				Shop.shopList.put(shops.get().shopId(), shopOwnerFinal);
			}, 10);
		} else {
			Util.sendMessage(player, "existing_shop");
		}
	}

	private void adminShop(Player player) {
		if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		if(!EventShop.adminShopEnabled) {
			Util.sendMessage(player, "admin_shop_disabled");
			return;
		}
		Block block = player.getTargetBlockExact(5);
		if(block == null) {
			Util.sendMessage(player, "target_mismatch");
			return;
		}
		String shopBlock = GoodTrade.config.getString("shopBlock");
		Material match = Material.matchMaterial(shopBlock);
		if(match == null) {
			try {
				match = Material.matchMaterial(shopBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception ignored) { }
			if(match == null)
				match = Material.BARREL;
		}
		if(!EventShop.multipleShopBlocks) {
			if(!block.getType().equals(match)) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		} else {
			boolean shopMatch = false;
			for(String shopBlocks:EventShop.multipleShopBlock) {
				Material shopListBlocks = Material.matchMaterial(shopBlocks);
				if(shopListBlocks != null && block.getType().equals(shopListBlocks)) {
					shopMatch = true;
					break;
				}
			}
			if(!block.getType().equals(match) && !shopMatch) {
				Util.sendMessage(player, "target_mismatch");
				return;
			}
		}
		Optional<Shop> shop = Shop.getShopByLocation(block.getLocation());
		if(shop.isPresent()) {
			Util.sendMessage(player, "existing_shop");
			return;
		}
		String adminHead;
		try { adminHead = GoodTrade.config.getString("adminPlayerHeadShops"); }
		catch(Exception e) { adminHead = "00000000-0000-0000-0000-000000000000"; }
		Shop newShop = Shop.createShop(block.getLocation(), UUID.fromString(adminHead), true);
		Util.sendMessage(player, "shop_created");
		if(GoodTrade.config.getBoolean("adminShopPublic")) {
			final String adminHeadFinal = adminHead;
			Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(GoodTrade.getPlugin(), () -> {
				Optional<Shop> shops = Shop.getShopByLocation(block.getLocation());
				Shop.shopList.put(shops.get().shopId(), UUID.fromString(adminHeadFinal));
			}, 10);
		}
		InvAdminShop inv = new InvAdminShop(newShop, player);
		inv.open(player, newShop.getOwner());
	}

	private void deleteShop(Player player) {
		Block block = player.getTargetBlockExact(5);
		if(block == null) {
			Util.sendMessage(player, "target_mismatch");
			return;
		}
		Optional<Shop> shop = Shop.getShopByLocation(block.getLocation());
		if(!shop.isPresent()) {
			Util.sendMessage(player, "no_shop_found");
			return;
		}
		if(!shop.get().isOwner(player.getUniqueId()) && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "shop_not_owned");
			return;
		}
		if(shop.get().isAdmin() && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		if(InvStock.inShopInv.containsValue(shop.get().getOwner())) {
			Util.sendMessage(player, "shop_busy");
			return;
		}
		double cost = GoodTrade.config.getDouble("returnAmount");
		Optional<Economy> economy = GoodTrade.getEconomy();
		if(cost > 0 && economy.isPresent()) {
			OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(shop.get().getOwner());
			economy.get().depositPlayer(offPlayer, cost);
		}
		Shop.shopList.remove(shop.get().shopId());
		shop.get().deleteShop();
		Util.sendMessage(player, "shop_deleted");
	}

	private void deleteShopID(Player player, String shopId) {
		int sID;
		try {
			sID = Integer.parseInt(shopId);
		} catch (Exception e) { sID = -1; }
		if(sID < 0) {
			Util.sendMessage(player, "shop_integer_error");
			return;
		}
		Optional<Shop> shop = Shop.getShopById(sID);
		if(!shop.isPresent()) {
			Util.sendMessage(player, "no_shop_found");
			return;
		}
		if(player != null && !shop.get().isOwner(player.getUniqueId()) && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "shop_not_owned");
			return;
		}
		if(shop.get().isAdmin() && player != null && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		if(InvStock.inShopInv.containsValue(shop.get().getOwner())) {
			Util.sendMessage(player, "shop_busy");
			return;
		}
		double cost = GoodTrade.config.getDouble("returnAmount");
		Optional<Economy> economy = GoodTrade.getEconomy();
		if(cost > 0 && economy.isPresent()) {
			OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(shop.get().getOwner());
			economy.get().depositPlayer(offPlayer, cost);
		}
		Shop.shopList.remove(shop.get().shopId());
		shop.get().deleteShop();
		Map<String, String> placeholders = new HashMap<>() {
			{
				this.put("id", shopId);
			}
		};
		Util.sendFormattedMessage(player, "shop_id_deleted", placeholders);
	}

	private void deleteLocation(Player player, String x, String y, String z, String worldString) {
		if(player != null && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		String world;
		if(worldString != null)
			world = worldString;
		else
			world = "world";

		int xLoc,yLoc,zLoc;
		try {
			xLoc = Integer.parseInt(x);
			yLoc = Integer.parseInt(y);
			zLoc = Integer.parseInt(z);
		}
		catch(Exception e) {
			Util.sendMessage(player, "shop_location_error_num");
			return;
		}
		World worldLoc = Bukkit.getWorld(world);
		Location blockLoc;
		if(worldLoc != null)
			blockLoc = new Location(worldLoc, xLoc, yLoc, zLoc);
		else {
			Util.sendMessage(player, "shop_location_error_world");
			return;
		}

		Optional<Shop> shop = Shop.getShopByLocation(blockLoc);
		if(!shop.isPresent()) {
			Util.sendMessage(player, "no_shop_found");
			return;
		}
		if(player != null && !shop.get().isOwner(player.getUniqueId()) && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "shop_not_owned");
			return;
		}
		if(player != null && shop.get().isAdmin() && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		if(InvStock.inShopInv.containsValue(shop.get().getOwner())) {
			Util.sendMessage(player, "shop_busy");
			return;
		}
		OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(shop.get().getOwner());
		String shopOwner = offPlayer.getName();
		double cost = GoodTrade.config.getDouble("returnAmount");
		Optional<Economy> economy = GoodTrade.getEconomy();
		if(cost > 0 && economy.isPresent())
			economy.get().depositPlayer(offPlayer, cost);
		Shop.shopList.remove(shop.get().shopId());
		shop.get().deleteShop();
		Map<String, String> placeholders = new HashMap<>() {
			{
				this.put("p", shopOwner);
				this.put("w", world);
				this.put("x", x);
				this.put("y", y);
				this.put("z", z);
			}
		};
		Util.sendFormattedMessage(player, "shop_deleted_loc", placeholders);
	}

	private void removeAllShops(Player player, String playerName) {
		if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		UUID sOwner;
		Player playerInGame = Bukkit.getPlayer(playerName);
		if(playerInGame != null && playerInGame.isOnline())
			sOwner = playerInGame.getUniqueId();
		else {
			try {
				sOwner = getUUID(playerName);
			} catch (Exception e) {
				UUID foundPlayerUUID = null;
				boolean foundPlayer = false;
				for(OfflinePlayer offlinePlayers : Bukkit.getOfflinePlayers())
					if(offlinePlayers.getName().equalsIgnoreCase(playerName)) {
						foundPlayerUUID = offlinePlayers.getUniqueId();
						foundPlayer = true;
						break;
					}
				if(!foundPlayer) {
					Util.sendMessage(player, "no_player_found");
					Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.RED) + "[GoodTrade] Player cannot be found!");
					return;
				}
				sOwner = foundPlayerUUID;
			}
		}
		if(InvStock.inShopInv.containsValue(sOwner)) {
			Util.sendMessage(player, "shop_busy");
			return;
		}
		final UUID shopOwner = sOwner;
		Bukkit.getServer().getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> Shop.removeAllPlayersShops(player, shopOwner, playerName));
	}

	private void listShops(Player player, String playerName) {
		if(playerName != null && !GoodTrade.config.getBoolean("publicListCommand") && (!player.hasPermission(Permission.SHOP_ADMIN.toString()) || !player.hasPermission(Permission.SHOP_LIST.toString()))) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		UUID sOwner;
		if(playerName == null) {
			sOwner = player.getUniqueId();
			playerName = player.getDisplayName();
		} else {
			Player playerInGame = Bukkit.getPlayer(playerName);
			if(playerInGame != null && playerInGame.isOnline())
				sOwner = playerInGame.getUniqueId();
			else {
				try {
					sOwner = getUUID(playerName);
				} catch (Exception e) {
					UUID foundPlayerUUID = null;
					boolean foundPlayer = false;
					for(OfflinePlayer offlinePlayers : Bukkit.getOfflinePlayers())
						if(offlinePlayers.getName().equalsIgnoreCase(playerName)) {
							foundPlayerUUID = offlinePlayers.getUniqueId();
							foundPlayer = true;
							break;
						}
					if(!foundPlayer) {
						Util.sendMessage(player, "no_player_found");
						Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.RED) + "[GoodTrade] Player cannot be found!");
						return;
					}
					sOwner = foundPlayerUUID;
				}
			}
		}
		Shop.getShopList(player, sOwner, playerName);
	}

	private void listAdminShops(Player player) {
		if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		Shop.getAdminShopList(player);
	}

	private void listAllShops(Player player) {
		if(!InvShop.listAllShops && (!player.hasPermission(Permission.SHOP_ADMIN.toString()) || !player.hasPermission(Permission.SHOP_SHOPS.toString()))) {
			Util.sendMessage(player, "shop_list_disabled");
			return;
		}
		InvShopList inv = InvShopList.setShopTitle(FormatUtil.replaceFormat(LocaleAPI.getMessage(player, "shop_list_title")));
		inv.setPag(0);
		inv.open(player);
	}

	private void stockShop(Player player, String page) {
		if(!InvAdminShop.stockCommandEnabled && !player.hasPermission(Permission.SHOP_ADMIN.toString()) && !player.hasPermission(Permission.SHOP_STOCK.toString())) {
			Util.sendMessage(player, "stock_command_disabled");
			return;
		}
		if(Shop.getNumShops(player.getUniqueId()) < 1 && GoodTrade.config.getBoolean("mustOwnShopForStock")) {
			Util.sendMessage(player, "no_shop_stock");
			return;
		}
		if(EventShop.stockRangeLimit > 0 && GoodTrade.config.getBoolean("stockRangeLimitUsingCommand") && !player.hasPermission(Permission.SHOP_ADMIN.toString()) && !player.hasPermission(Permission.SHOP_STOCK.toString()))
			if(!Shop.checkShopDistanceFromStockBlock(player.getLocation(), player.getUniqueId())) {
				Util.sendMessage(player, "out_of_stock_range");
				return;
			}
		int openPage;
		try { openPage = Integer.parseInt(page); }
		catch(Exception e) { openPage = 1; }
		if(openPage < 1) {
			Util.sendMessage(player, "stock_integer_error");
			return;
		}
		openPage--;
		int maxStockPages = InvAdminShop.maxPages;
		if(InvAdminShop.usePerms) {
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
		if(openPage > 0 && openPage > maxStockPages-1)
			openPage = maxStockPages-1;
		if(InvStock.inShopInv.containsValue(player.getUniqueId())) {
			Util.sendMessage(player, "shop_busy");
			return;
		} else { InvStock.inShopInv.put(player, player.getUniqueId()); }
		InvStock inv = InvStock.getInvStock(player.getUniqueId(), player);
		inv.setMaxPages(maxStockPages);
		inv.setPag(openPage);
		inv.open(player);
	}

	private void reloadShop(Player player) {
		if(player != null && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		GoodTrade plugin = (GoodTrade) Bukkit.getPluginManager().getPlugin("GoodTrade");
		if(plugin != null)
			plugin.createConfig();
		if (player != null) {
			player.sendMessage(ChatColor.GREEN + "[GoodTrade] Configuration file reloaded.");
		} else {
			Bukkit.getConsoleSender().sendMessage("[GoodTrade] Configuration file reloaded.");
		}
		Bukkit.getScheduler().runTaskAsynchronously(GoodTrade.getPlugin(), () -> {
			EventShop.adminShopEnabled = GoodTrade.config.getBoolean("enableAdminShop");
			EventShop.noShopNoStock = GoodTrade.config.getBoolean("mustOwnShopForStock");
			EventShop.placeFrameSign = GoodTrade.config.getBoolean("placeItemFrameSigns");
			EventShop.protectShopFromExplosion = GoodTrade.config.getBoolean("protectShopBlocksFromExplosions");
			EventShop.shopBlock = GoodTrade.config.getString("shopBlock");
			EventShop.stockBlock = GoodTrade.config.getString("stockBlock");
			EventShop.stockEnabled = GoodTrade.config.getBoolean("enableStockBlock");
			EventShop.shopEnabled = GoodTrade.config.getBoolean("enableShopBlock");
			EventShop.shopBlk = Material.matchMaterial(EventShop.shopBlock);
			EventShop.stockBlk = Material.matchMaterial(EventShop.stockBlock);
			EventShop.stockRangeLimit = GoodTrade.config.getInt("stockRangeLimitFromShop");
			EventShop.soldJoinMessage = GoodTrade.config.getBoolean("enableSoldNotificationOnJoin");
			EventShop.soldOnlyOnFirstConnect = GoodTrade.config.getBoolean("onlyNotifySoldOnceUntilClear");
			EventShop.soldMessageDelayTime = GoodTrade.config.getInt("soldNotificationsDelayTime");
			EventShop.multipleShopBlock = GoodTrade.config.getStringList("shopBlockList");
			EventShop.multipleStockBlock = GoodTrade.config.getStringList("stockBlockList");
			EventShop.multipleShopBlocks = GoodTrade.config.getBoolean("multipleShopBlocks");
			EventShop.multipleStockBlocks = GoodTrade.config.getBoolean("multipleStockBlocks");
			InvAdminShop.remoteManage = GoodTrade.config.getBoolean("remoteManage");
			InvAdminShop.maxPages = GoodTrade.config.getInt("stockPages");
			InvAdminShop.permissionMax = GoodTrade.config.getInt("maxStockPages");
			InvAdminShop.stockCommandEnabled = GoodTrade.config.getBoolean("enableStockCommand");
			InvAdminShop.stockGUIShop = GoodTrade.config.getBoolean("enableStockAccessFromShopGUI");
			InvAdminShop.usePerms = GoodTrade.config.getBoolean("usePermissions");
			InvCreateRow.disabledItemList = GoodTrade.config.getStringList("disabledItemsList");
			InvCreateRow.itemsDisabled = GoodTrade.config.getBoolean("disabledItems");
			InvCreateRow.preventDupeTrades = GoodTrade.config.getBoolean("preventDuplicates");
			InvCreateRow.preventAllDupeTrades = GoodTrade.config.getBoolean("preventAllDuplicates");
			InvCreateRow.strictStock = GoodTrade.config.getBoolean("strictStock");
			InvShop.listAllShops = GoodTrade.config.getBoolean("publicShopListCommand");
			Shop.showOwnedShops = GoodTrade.config.getBoolean("publicShopListShowsOwned");
			Shop.shopEnabled = GoodTrade.config.getBoolean("enableShopBlock");
			Shop.shopNotifications = GoodTrade.config.getBoolean("enableShopNotifications");
			Shop.shopOutStock = GoodTrade.config.getBoolean("enableOutOfStockMessages");
			Shop.particleEffects = GoodTrade.config.getBoolean("showParticles");
			Shop.maxDays = GoodTrade.config.getInt("maxInactiveDays");
			Shop.deletePlayerShop = GoodTrade.config.getBoolean("deleteBlock");
			Shop.saveEmptyShops = GoodTrade.config.getBoolean("saveEmptyShops");
			Shop.stockMessages = GoodTrade.config.getBoolean("enableShopSoldMessage");
			Shop.exemptExpiringList = GoodTrade.config.getStringList("exemptExpiringShops");
		});
	}

	private static UUID getUUID(String name) throws Exception {
		Scanner scanner = new Scanner(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream());
		String input = scanner.nextLine();
		scanner.close();
		JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(input);
		String uuidString = UUIDObject.get("id").toString();
		String uuidSeparation = uuidString.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
		return UUID.fromString(uuidSeparation);
	}

	private static void shopManage(Player player, String shopID) {
		if(!InvAdminShop.remoteManage && (!player.hasPermission(Permission.SHOP_ADMIN.toString()) || !player.hasPermission(Permission.SHOP_REMOTEMANAGE.toString()))) {
			Util.sendMessage(player, "no_remote_manage");
			return;
		}
		int shopId;
		try {
			shopId = Integer.parseInt(shopID);
		} catch (Exception e) { shopId = -1; }
		if(shopId < 0) {
			Util.sendMessage(player, "shop_not_owned");
			return;
		}
		Optional<Shop> shop = Shop.getShopById(shopId);
		if(!shop.isPresent() || (!shop.get().isOwner(player.getUniqueId()) && !player.hasPermission(Permission.SHOP_ADMIN.toString()))) {
			Util.sendMessage(player, "shop_not_owned");
			return;
		}
		if(shop.get().isAdmin()) {
			if(!EventShop.adminShopEnabled) {
				Util.sendMessage(player, "admin_shop_disabled");
				return;
			}
			if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
				Util.sendMessage(player, "no_permissions");
				return;
			}
		}
		if(InvStock.inShopInv.containsValue(shop.get().getOwner())) {
			Util.sendMessage(player, "shop_busy");
			return;
		}
		InvAdminShop inv = new InvAdminShop(shop.get(), player);
		inv.open(player, shop.get().getOwner());
	}

	private static void viewShop(Player player, String shopId) {
		if(!GoodTrade.config.getBoolean("remoteShopping") && (!player.hasPermission(Permission.SHOP_ADMIN.toString()) || !player.hasPermission(Permission.SHOP_REMOTESHOPPING.toString()))) {
			Util.sendMessage(player, "no_remote_manage");
			return;
		}
		int sID;
		try {
			sID = Integer.parseInt(shopId);
		} catch (Exception e) { sID = -1; }
		if(sID < 0) {
			Util.sendMessage(player, "shop_integer_error");
			return;
		}
		Optional<Shop> shop = Shop.getShopById(sID);
		if(!shop.isPresent()) {
			Util.sendMessage(player, "no_shop_found");
			return;
		}
		if(shop.get().getOwner().equals(player.getUniqueId()) && !InvAdminShop.remoteManage && (!player.hasPermission(Permission.SHOP_ADMIN.toString()) || !player.hasPermission(Permission.SHOP_REMOTEMANAGE.toString()))) {
			Util.sendMessage(player, "no_remote_manage");
			return;
		}
		if(shop.get().isAdmin() && !EventShop.adminShopEnabled) {
			Util.sendMessage(player, "admin_shop_disabled");
			return;
		}
		if(InvStock.inShopInv.containsValue(shop.get().getOwner())) {
			Util.sendMessage(player, "shop_busy");
			return;
		}
		if((shop.get().isAdmin() && (player.hasPermission(Permission.SHOP_ADMIN.toString())) || shop.get().isOwner(player.getUniqueId()))) {
			InvAdminShop inv = new InvAdminShop(shop.get(), player);
			inv.open(player, shop.get().getOwner());
		} else {
			InvShop inv = new InvShop(shop.get(), player);
			inv.open(player, shop.get().getOwner());
		}
	}

	private void manageStock(Player player, String stockOwner, String page) {
		if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		UUID sOwner;
		if(stockOwner == null) {
			Util.sendMessage(player, "no_player_found");
			return;
		} else {
			Player playerInGame = Bukkit.getPlayer(stockOwner);
			if(playerInGame != null && playerInGame.isOnline())
				sOwner = playerInGame.getUniqueId();
			else {
				try {
					sOwner = getUUID(stockOwner);
				} catch (Exception e) {
					UUID foundPlayerUUID = null;
					boolean foundPlayer = false;
					for(OfflinePlayer offlinePlayers : Bukkit.getOfflinePlayers())
						if(offlinePlayers.getName().equalsIgnoreCase(stockOwner)) {
							foundPlayerUUID = offlinePlayers.getUniqueId();
							foundPlayer = true;
							break;
						}
					if(!foundPlayer) {
						Util.sendMessage(player, "no_player_found");
						Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.RED) + "[GoodTrade] Player cannot be found!");
						return;
					}
					sOwner = foundPlayerUUID;
				}
			}
		}
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(sOwner);
		if(offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
			Util.sendMessage(player, "no_player_found");
			return;
		}
		int pageNum;
		try { pageNum = Integer.parseInt(page); }
		catch(Exception e) { pageNum = 1; }
		if(pageNum < 1) {
			Util.sendMessage(player, "stock_integer_error");
			return;
		}
		pageNum--;
		int maxStockPages;
		if(InvAdminShop.usePerms)
			maxStockPages = InvAdminShop.permissionMax;
		else
			maxStockPages = InvAdminShop.maxPages;
		if(pageNum > 0 && pageNum > maxStockPages-1)
			pageNum = maxStockPages-1;
		final int openPage = pageNum;
		final int stockPage = maxStockPages;
		final UUID shopOwner = sOwner;
		Bukkit.getScheduler().runTask(GoodTrade.getPlugin(), () -> {
			if(InvStock.inShopInv.containsValue(shopOwner)) {
				Util.sendMessage(player, "shop_busy");
				return;
			} else
				InvStock.inShopInv.put(player, shopOwner);
			InvStock inv = InvStock.getInvStock(shopOwner, player);
			inv.setMaxPages(stockPage);
			inv.setPag(openPage);
			inv.open(player);
		});
	}

	private void shopSold(Player player, String clearOrPageNumber) {
		if(Shop.stockMessages) {
			int pageNum;
			if(clearOrPageNumber == null)
				pageNum=1;
			else if(clearOrPageNumber.equals("clear") && Shop.shopMessages.containsKey(player.getUniqueId())) {
				Shop.shopMessages.remove(player.getUniqueId());
				EventShop.soldListSent.remove(player.getUniqueId());
				Util.sendMessage(player, "sold_clear");
				return;
			}
			else {
				try {
					pageNum = Integer.parseInt(clearOrPageNumber);
				} catch (Exception e) {
					pageNum=1;
				}
				if(pageNum<1) {
					Util.sendMessage(player, "sold_integer_error");
					return;
				}
			}
			if(Shop.shopMessages.containsKey(player.getUniqueId())) {
				List<String> messages = Shop.shopMessages.get(player.getUniqueId());
				int msgSize = messages.size();
				int maxSoldPages = (int)Math.ceil(msgSize/5);
				if(pageNum>maxSoldPages)
					pageNum=maxSoldPages+1;
				int finalPageNum = pageNum;
				Map<String, String> placeholders = new HashMap<String, String>() {
					{
						this.put("p", String.valueOf(finalPageNum));
					}
				};
				Util.sendFormattedMessage(player, "sold_header", placeholders);
				pageNum--;
				if(msgSize<6) {
					for(String msg:messages)
						player.sendMessage(msg);
					if(GoodTrade.config.getBoolean("autoClearSoldListOnLast")) {
						Shop.shopMessages.remove(player.getUniqueId());
						EventShop.soldListSent.remove(player.getUniqueId());
					}
				} else {
					int index;
					if(pageNum>0)
						index=pageNum*5;
					else
						index=0;
					for(int i=0; i<5; i++)
						if(index<=msgSize-1) {
							player.sendMessage(messages.get(index));
							index++;
						}
					if(index<6) {
						int pageNext = pageNum+2;
						int currentPage = pageNum+1;
						String soldPages = LocaleAPI.getMessage(player, "sold_pages").replaceAll("%p", String.valueOf(currentPage));
						TextComponent soldMsg = new TextComponent(soldPages);
						TextComponent pageNextText = new TextComponent(LocaleAPI.getMessage(player, "sold_pages_next"));
						pageNextText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shop sold " + pageNext));
						soldMsg.addExtra(pageNextText);
						player.spigot().sendMessage(soldMsg);
					} else if(index<msgSize-1) {
						int pageNext = pageNum+2;
						int currentPage = pageNum+1;
						String prevPage = "";
						TextComponent totalMsg = new TextComponent(prevPage);
						TextComponent pagePrevText = new TextComponent(LocaleAPI.getMessage(player, "sold_pages_previous"));
						pagePrevText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shop sold " + pageNum));
						String soldPages = LocaleAPI.getMessage(player, "sold_pages").replaceAll("%p", String.valueOf(currentPage));
						TextComponent soldMsg = new TextComponent(soldPages);
						TextComponent pageNextText = new TextComponent(LocaleAPI.getMessage(player, "sold_pages_next"));
						pageNextText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shop sold " + pageNext));
						totalMsg.addExtra(pagePrevText);
						totalMsg.addExtra(soldMsg);
						totalMsg.addExtra(pageNextText);
						player.spigot().sendMessage(totalMsg);
					} else {
						if(GoodTrade.config.getBoolean("autoClearSoldListOnLast")) {
							Shop.shopMessages.remove(player.getUniqueId());
							EventShop.soldListSent.remove(player.getUniqueId());
						} else {
							int currentPage = pageNum+1;
							String prevPage = "";
							TextComponent totalMsg = new TextComponent(prevPage);
							TextComponent pagePrevText = new TextComponent(LocaleAPI.getMessage(player, "sold_pages_previous"));
							pagePrevText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/shop sold " + pageNum));
							String soldPages = LocaleAPI.getMessage(player, "sold_pages").replaceAll("%p", String.valueOf(currentPage));
							TextComponent soldMsg = new TextComponent(soldPages);
							totalMsg.addExtra(pagePrevText);
							totalMsg.addExtra(soldMsg);
							player.spigot().sendMessage(totalMsg);
						}
					}
				}
			} else {
				Util.sendMessage(player, "sold_nothing");
			}
		} else {
			Util.sendMessage(player, "sold_command_disabled");
		}
	}

	private void outOfStock(Player player, String playerName) {
		if(playerName != null && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			Util.sendMessage(player, "no_permissions");
			return;
		}
		UUID sOwner;
		if(playerName == null) {
			sOwner = player.getUniqueId();
			playerName = player.getDisplayName();
		} else {
			Player playerInGame = Bukkit.getPlayer(playerName);
			if(playerInGame != null && playerInGame.isOnline())
				sOwner = playerInGame.getUniqueId();
			else {
				try {
					sOwner = getUUID(playerName);
				} catch (Exception e) {
					UUID foundPlayerUUID = null;
					boolean foundPlayer = false;
					for(OfflinePlayer offlinePlayers : Bukkit.getOfflinePlayers())
						if(offlinePlayers.getName().equalsIgnoreCase(playerName)) {
							foundPlayerUUID = offlinePlayers.getUniqueId();
							foundPlayer = true;
							break;
						}
					if(!foundPlayer) {
						Util.sendMessage(player, "no_player_found");
						Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.RED) + "[GoodTrade] Player cannot be found!");
						return;
					}
					sOwner = foundPlayerUUID;
				}
			}
		}
		Util.sendMessage(player, "list_out_of_stock");
		Shop.getOutOfStock(player, sOwner, playerName);
	}
}
