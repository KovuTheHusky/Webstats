package com.codeski.webstats.databases;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.codeski.webstats.Webstats;

public abstract class Database {
	private static int queries = 0, updates = 0, interval = 60;
	protected static Connection connection;

	public abstract boolean connect(FileConfiguration configuration);

	public void debugTimer() {
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Bukkit.broadcastMessage(ChatColor.GREEN + "" + queries + " queries and " + updates + " updates in last " + interval + " seconds.");
				queries = updates = 0;
			}
		}, interval * 1000, interval * 1000);
	}

	public abstract void disconnect();

	public abstract void materialEvent(String player, String event, String type, String count, String world, String x, String y, String z);

	public abstract void playerJoined(String player, String uuid);

	public abstract void playerQuit(String player);

	public Result query(String sql) {
		++queries;
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
		++updates;
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

	public int update_old(String sql) {
		Bukkit.broadcastMessage(ChatColor.RED + "OLD: " + sql);
		return 0;
	}
}
