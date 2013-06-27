package com.codeski.webstats;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Webstats extends JavaPlugin {
	private static final BlockListener blockListener = new BlockListener();
	private static FileConfiguration configuration;
	private static File directory;
	private static Logger logger;
	private static final PlayerListener playerListener = new PlayerListener();
	private static Server server;

	public static void broadcast(String str) {
		server.broadcastMessage(str);
	}

	public static void debug(String str) {
		if (configuration.getBoolean("developer.debugging"))
			logger.info(str);
	}

	public static FileConfiguration getConfiguration() {
		return configuration;
	}

	public static File getDirectory() {
		return directory;
	}

	public static void info(String str) {
		logger.info(str);
	}

	public static void severe(String str) {
		logger.severe(str);
	}

	public static void warning(String str) {
		logger.warning(str);
	}

	@Override
	public void onDisable() {
		Database.disconnect();
	}

	@Override
	public void onEnable() {
		server = this.getServer();
		logger = this.getLogger();
		this.saveDefaultConfig();
		configuration = this.getConfig();
		configuration.options().copyDefaults(true);
		this.saveConfig();
		directory = this.getDataFolder();
		if (!Database.connect())
			return;
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(blockListener, this);
	}
}
