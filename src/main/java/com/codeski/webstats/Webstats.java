package com.codeski.webstats;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.codeski.webstats.databases.Database;
import com.codeski.webstats.databases.MYSQLDatabase;

public class Webstats extends JavaPlugin {
	private static FileConfiguration configuration;
	private static Database database;
	// private static File directory;
	private static Logger logger;
	private static Server server;

	public static void broadcast(String msg) {
		server.broadcastMessage(msg);
	}

	public static void debug(String msg) {
		if (configuration.getBoolean("developer.debugging"))
			logger.info(msg);
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void severe(String msg) {
		logger.severe(msg);
	}

	public static void warning(String msg) {
		logger.warning(msg);
	}

	@Override
	public void onDisable() {
		database.disconnect();
	}

	@Override
	public void onEnable() {
		server = this.getServer();
		logger = this.getLogger();
		this.saveDefaultConfig();
		configuration = this.getConfig();
		configuration.options().copyDefaults(true);
		this.saveConfig();
		if (!configuration.getBoolean("advanced.enabled")) {
			String d = configuration.getString("database.driver");
			if (d.equalsIgnoreCase("MYSQL"))
				database = new MYSQLDatabase();
			else if (d.equalsIgnoreCase("POSTGRESQL"))
				database = null;
			else if (d.equalsIgnoreCase("SQLITE"))
				database = null;
		} else
			database = null;
		// directory = this.getDataFolder();
		if (!database.connect(configuration))
			return;
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(database), this);
		pm.registerEvents(new BlockListener(database), this);
	}
}
