package com.codeski.webstats.databases;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.codeski.webstats.Webstats;

public abstract class Database {
	protected static Connection connection;

	public abstract void blockBroken(Player player, int block, byte data);

	public abstract void blockPlaced(Player player, int block, byte data);

	public abstract boolean connect(FileConfiguration configuration);

	public abstract void disconnect();

	public abstract void distanceByBoat(Player player, double distance);

	public abstract void distanceByHorse(Player player, double distance);

	public abstract void distanceByMinecart(Player player, double distance);

	public abstract void distanceByPig(Player player, double distance);

	public abstract void distanceClimbed(Player player, double distance);

	public abstract void distanceDove(Player player, double distance);

	public abstract void distanceFallen(Player player, double distance);

	public abstract void distanceFlown(Player player, double distance);

	public abstract void distanceSwam(Player player, double distance);

	public abstract void distanceWalked(Player player, double distance);

	public abstract void playerEggThrown(Player player, int spawn);

	public abstract void playerFish(Player player, boolean caught);

	public abstract void playerJoined(Player player);

	public abstract void playerQuit(Player player);

	public Result query(String sql) {
		Webstats.debug(sql);
		try {
			Statement s = connection.createStatement();
			return new Result(s, s.executeQuery(sql));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int update(String sql) {
		Webstats.debug(sql);
		int ret = -1;
		Statement s = null;
		try {
			s = connection.createStatement();
			ret = s.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (SQLException ignore) {
			}
		}
		return ret;
	}
}
