package com.codeski.webstats.databases;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.codeski.webstats.Webstats;

public class PGSQLDatabase extends Database {
	private final String[] tables = {};

	@Override
	public void addDistance(String player, String type, String count, String world, String fromX, String fromY, String fromZ, String toX, String toY, String toZ) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addMaterial(String player, String event, String type, String count, String world, String x, String y, String z) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean connect(FileConfiguration configuration) {
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://" + configuration.getString("database.host") + "/" + configuration.getString("database.database"), configuration.getString("database.user"), configuration.getString("database.pass"));
		} catch (ClassNotFoundException e) {
			Webstats.severe("Unable to load database driver. Your configuration may be incorrect.");
			return false;
		} catch (SQLException e) {
			Webstats.severe("Unable to connect to database. Your configuration may be incorrect.");
			return false;
		}
		for (String sql : tables)
			this.update(sql);
		this.update("TRUNCATE ws_materials");
		for (Material m : Material.values())
			this.update("INSERT INTO ws_materials VALUES (NULL, '" + m + "')");
		return true;
	}

	@Override
	public void disconnect() {
		try {
			connection.close();
		} catch (SQLException ignore) {
		}
	}

	@Override
	public void playerJoined(String player, String uuid) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerQuit(String player) {
		// TODO Auto-generated method stub
	}
}
