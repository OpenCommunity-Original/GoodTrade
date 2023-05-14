package com.opencommunity.goodtrade;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import com.opencommunity.goodtrade.gui.GUIEvent;
import com.opencommunity.goodtrade.utils.LocaleAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitTask;

public class GoodTrade extends JavaPlugin {
	public static FileConfiguration config;
	private static BukkitTask expiredTask;
	private static BukkitTask saveTask;
	private static BukkitTask tickTask;
	private static Connection connection = null;
	private static Economy economy = null;
	private static String chainConnect;
	File configFile;

	public GoodTrade() {
	}

	public static Connection getConnection() {
		checkConnection();
		return connection;
	}

	public static void checkConnection() {
		try {
			if (connection == null || connection.isClosed() || !connection.isValid(0)) {
				connection = DriverManager.getConnection(chainConnect);
			}
		} catch (Exception var1) {
			var1.printStackTrace();
		}

	}

	public static Optional<Economy> getEconomy() {
		return Optional.ofNullable(economy);
	}

	public static GoodTrade getPlugin() {
		return GoodTrade.getPlugin(GoodTrade.class);
	}

	public void onEnable() {
		chainConnect = "jdbc:sqlite:" + this.getDataFolder().getAbsolutePath() + "/shops.db";
		this.setupEconomy();
		this.createConfig();
		if (config.getString("shopBlock") == null) {
			config.set("shopBlock", "minecraft:barrel");
			Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.RED) + "[GoodTrade] Block cannot be empty! Reverting to default minecraft:barrel");
		}

		if (config.getString("stockBlock") == null) {
			config.set("stockBlock", "minecraft:composter");
			Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.RED) + "[GoodTrade] stockBlock cannot be empty! Reverting to default minecraft:composter");
		}

		this.getServer().getPluginManager().registerEvents(new EventShop(), this);
		this.getServer().getPluginManager().registerEvents(new GUIEvent(), this);
		this.getCommand("goodtrade").setExecutor(new CommandShop());

		int delayTime;
		try {
			Class.forName("org.sqlite.JDBC");
			delayTime = config.getInt("shopsDatabaseLoadDelay");
		} catch (Exception var5) {
			delayTime = 0;
		}

		if (delayTime < 1) {
			delayTime = 1;
		} else {
			delayTime *= 20;
		}

		int saveDatabaseTime;
		try {
			saveDatabaseTime = config.getInt("saveDatabase");
		} catch (Exception var4) {
			saveDatabaseTime = 15;
		}

		if (saveDatabaseTime < 5) {
			saveDatabaseTime = 5;
		}

		saveDatabaseTime *= 60;
		saveDatabaseTime *= 20;
		Bukkit.getScheduler().runTaskLater(this, () -> {
			try {
				connection = DriverManager.getConnection(chainConnect);
				this.createTables();
				Shop.loadData();
			} catch (Exception var2) {
				var2.printStackTrace();
			}

		}, (long)delayTime);
		expiredTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, Shop::expiredShops, (long)(delayTime + 9), 20000L);
		saveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			try {
				Shop.saveData(false);
			} catch (Exception var1) {
				Bukkit.getConsoleSender().sendMessage(String.valueOf(ChatColor.YELLOW) + "[GoodTrade] Warning: Tried saving to database while being modified at the same time. Saving will continue and try again later, but will also save to database safely upon server shutdown.");
			}

		}, (long)(delayTime + 1200), (long)saveDatabaseTime);
		if (Shop.shopEnabled && Shop.particleEffects) {
			tickTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, Shop::tickShops, (long)(delayTime + 250), 50L);
		}

		Bukkit.getScheduler().runTaskLaterAsynchronously(this, Shop::getPlayersShopList, (long)(delayTime + 160));
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, Shop::removeEmptyShopTrade, (long)(delayTime + 100));
		LocaleAPI localeAPI = new LocaleAPI();
		Bukkit.getPluginManager().registerEvents(localeAPI, this);
		localeAPI.loadSupportedLocales(this);
	}

	public void onDisable() {
		expiredTask.cancel();
		saveTask.cancel();
		tickTask.cancel();
		this.getServer().getConsoleSender().sendMessage(String.valueOf(ChatColor.GREEN) + "[GoodTrade] Safely saving shops & stock items to database, please wait & do not kill server process...");
		Shop.saveData(true);
		this.getServer().getConsoleSender().sendMessage(String.valueOf(ChatColor.GREEN) + "[GoodTrade] Saving complete!");
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException var2) {
				var2.printStackTrace();
			}
		}

	}

	private void createTables() {
		PreparedStatement[] stmts = new PreparedStatement[0];

		try {
			stmts = new PreparedStatement[]{connection.prepareStatement("CREATE TABLE IF NOT EXISTS zooMercaTiendas(id INTEGER PRIMARY KEY autoincrement, location varchar(64), owner varchar(64));"), connection.prepareStatement("CREATE TABLE IF NOT EXISTS zooMercaTiendasFilas(itemIn text, itemOut text, idTienda INTEGER);"), connection.prepareStatement("CREATE TABLE IF NOT EXISTS zooMercaStocks(owner varchar(64), items JSON);")};
		} catch (Exception var10) {
			var10.printStackTrace();
		}

		PreparedStatement[] var2 = stmts;
		int var3 = stmts.length;

		for(int var4 = 0; var4 < var3; ++var4) {
			PreparedStatement stmt = var2[var4];

			try {
				stmt.execute();
				stmt.close();
			} catch (Exception var9) {
				var9.printStackTrace();
			}
		}

		List<PreparedStatement> stmtsPatches = new ArrayList();

		try {
			stmtsPatches.add(connection.prepareStatement("ALTER TABLE zooMercaTiendasFilas ADD COLUMN itemIn2 text NULL DEFAULT 'v: 2580\ntype: AIR\namount: 0' "));
			stmtsPatches.add(connection.prepareStatement("ALTER TABLE zooMercaTiendasFilas ADD COLUMN itemOut2 text NULL DEFAULT 'v: 2580\ntype: AIR\namount: 0' "));
			stmtsPatches.add(connection.prepareStatement("ALTER TABLE zooMercaTiendasFilas ADD COLUMN broadcast BOOLEAN DEFAULT 0"));
			stmtsPatches.add(connection.prepareStatement("ALTER TABLE zooMercaStocks ADD COLUMN pag INTEGER DEFAULT 0"));
			stmtsPatches.add(connection.prepareStatement("ALTER TABLE zooMercaTiendas ADD COLUMN admin BOOLEAN DEFAULT FALSE;"));
		} catch (Exception var8) {
		}

		Iterator var12 = stmtsPatches.iterator();

		while(var12.hasNext()) {
			PreparedStatement stmtsPatch = (PreparedStatement)var12.next();

			try {
				stmtsPatch.execute();
				stmtsPatch.close();
			} catch (Exception var7) {
			}
		}

	}

	private void setupEconomy() {
		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp != null) {
				economy = (Economy)rsp.getProvider();
			}
		}
	}

	public void createConfig() {
		this.configFile = new File(this.getDataFolder(), "config.yml");
		if (!this.configFile.exists()) {
			this.configFile.getParentFile().mkdirs();
			this.saveResource("config.yml", false);
		}

		config = new YamlConfiguration();

		try {
			config.load(this.configFile);
			String ver = config.getString("configVersion");
			if (ver.equals("3.7")) {
			}
		} catch (InvalidConfigurationException | IOException var2) {
			var2.printStackTrace();
		}

	}
}