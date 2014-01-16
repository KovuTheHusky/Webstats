package com.codeski.webstats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.codeski.webstats.databases.Database;
import com.codeski.webstats.databases.MYSQLDatabase;

public class Webstats extends JavaPlugin {
	private static FileConfiguration configuration;
	private static Database database;
	private static Logger logger;

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
		logger = this.getLogger();
		// Figure out the configuration
		this.saveDefaultConfig();
		configuration = this.getConfig();
		configuration.options().copyDefaults(true);
		this.saveConfig();
		// Update extracted files if needed
		// TODO: Check version.txt beforehand
		File df = this.getDataFolder();
		if (!df.exists())
			df.mkdirs();
		try {
			ZipFile zf = new ZipFile(new File(Webstats.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
			FileOutputStream fos = null;
			InputStream ins = null;
			byte[] buf = new byte[2048];
			File f;
			String n;
			Enumeration<? extends ZipEntry> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				n = ze.getName();
				if (!n.startsWith("extracted/"))
					continue;
				n = n.substring("extracted/".length());
				f = new File(df, n);
				if (ze.isDirectory())
					f.mkdirs();
				else {
					f.getParentFile().mkdirs();
					fos = new FileOutputStream(f);
					ins = zf.getInputStream(ze);
					int len;
					while ((len = ins.read(buf)) >= 0)
						fos.write(buf, 0, len);
					ins.close();
					fos.close();
				}
			}
			zf.close();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		// Get the database going
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
		if (!database.connect(configuration))
			return;
		// Start listening for events
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new PlayerListener(database), this);
		pm.registerEvents(new BlockListener(database), this);
	}
}
