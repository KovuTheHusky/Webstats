package com.codeski.webstats.databases;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.codeski.webstats.Webstats;

public class PGSQLDatabase extends Database {
	private final String[] tables = {};

	@Override
	public void blockBroken(Player player, int block, byte data) {
		// TODO Auto-generated method stub
	}

	@Override
	public void blockPlaced(Player player, int block, byte data) {
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
			this.update("INSERT INTO ws_materials VALUES (" + m.getId() + ", '" + m + "')");
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
	public void distanceByBoat(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceByHorse(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceByMinecart(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceByPig(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceClimbed(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceDove(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceFallen(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceFlown(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceSwam(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void distanceWalked(Player player, double distance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerEggThrown(Player player, int spawn) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerFish(Player player, boolean caught) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerJoined(Player player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerQuit(Player player) {
		// TODO Auto-generated method stub
	}
}