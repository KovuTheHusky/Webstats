package com.codeski.webstats.databases;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.configuration.file.FileConfiguration;

import com.codeski.webstats.Webstats;

public abstract class Database {
	protected static Connection connection;

	public abstract void addMaterial(String player, String event, String type, String count, String world, String x, String y, String z);

	public abstract boolean connect(FileConfiguration configuration);

	public abstract void disconnect();

	public abstract void playerJoined(String player, String uuid);

	public abstract void playerQuit(String player);

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
