package com.codeski.webstats;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Material;

public class Database {
	public static final byte MYSQL = 0;
	public static final byte OTHER = Byte.MAX_VALUE;
	public static final byte POSTGRESQL = 1;
	public static final byte SQLITE = 2;
	private static Connection connection;
	private static byte driver = -1;
	private static final String[][] tables = { {
			"CREATE TABLE IF NOT EXISTS ws_blocks (block_id INTEGER, block_data INTEGER, block_broken INTEGER, block_crafted INTEGER, block_placed INTEGER, block_player CHAR(36))",
			"CREATE TABLE IF NOT EXISTS ws_damages (damage_type INTEGER, damage_damager_type INTEGER, damage_damager_id CHAR(36), damage_damaged_type INTEGER, damage_damaged_id CHAR(36))",
			"CREATE TABLE IF NOT EXISTS ws_distances (distance_walked DOUBLE, distance_swam DOUBLE, distance_fallen DOUBLE, distance_climbed DOUBLE, distance_flown DOUBLE, distance_dove DOUBLE, distance_by_minecart DOUBLE, distance_by_boat DOUBLE, distance_by_pig DOUBLE, distance_by_horse DOUBLE, distance_player CHAR(36))",
			"CREATE TABLE IF NOT EXISTS ws_eggs (egg_hatch INTEGER, egg_hatch_count INTEGER, egg_player CHAR(36))",
			"CREATE TABLE IF NOT EXISTS ws_fishes (fish_casts INTEGER, fish_catches INTEGER, fish_player CHAR(36))",
			"CREATE TABLE IF NOT EXISTS ws_items (item_id INTEGER, item_broken INTEGER, item_crafted INTEGER, item_dropped INTEGER, item_picked_up INTEGER, item_used INTEGER, item_player CHAR(36))",
			"CREATE TABLE IF NOT EXISTS ws_materials (material_id INTEGER PRIMARY KEY, material_name CHAR(32))",
			"CREATE TABLE IF NOT EXISTS ws_players (player_id CHAR(36) PRIMARY KEY, player_name CHAR(16) UNIQUE, player_arrows INTEGER, player_beds_entered INTEGER, player_beds_left INTEGER, player_commands INTEGER, player_ender INTEGER, player_first INTEGER, player_joins INTEGER, player_jumps INTEGER, player_kicks INTEGER, player_last INTEGER, player_longest INTEGER, player_online INTEGER, player_played INTEGER, player_portals INTEGER, player_quits INTEGER, player_words INTEGER)",
			"CREATE TABLE IF NOT EXISTS ws_statistics (statistic_first INTEGER, statistic_startup INTEGER, statistic_shutdown INTEGER, statistic_uptime DOUBLE)"
	}, {
			""
	}, {
			""
	} };

	public static boolean connect() {
		// Figure out what driver we are using
		String d = Webstats.getConfiguration().getString("database.driver");
		if (!Webstats.getConfiguration().getBoolean("advanced.enabled"))
			if (d.equalsIgnoreCase("MYSQL"))
				driver = MYSQL;
			else if (d.equalsIgnoreCase("POSTGRESQL"))
				driver = POSTGRESQL;
			else if (d.equalsIgnoreCase("SQLITE"))
				driver = SQLITE;
			else
				driver = OTHER;
		// Connect to the database specified
		try {
			switch (driver) {
				case MYSQL:
					Class.forName("com.mysql.jdbc.Driver");
					connection = DriverManager.getConnection("jdbc:mysql://" + Webstats.getConfiguration().getString("database.host") + "/" + Webstats.getConfiguration().getString("database.database") + "?user=" + Webstats.getConfiguration().getString("database.user") + "&password=" + Webstats.getConfiguration().getString("database.pass"));
					break;
				case POSTGRESQL:
					Class.forName("org.postgresql.Driver");
					connection = DriverManager.getConnection("jdbc:postgresql://" + Webstats.getConfiguration().getString("database.host") + "/" + Webstats.getConfiguration().getString("database.database"), Webstats.getConfiguration().getString("database.user"), Webstats.getConfiguration().getString("database.pass"));
					break;
				case SQLITE:
					Class.forName("org.sqlite.JDBC");
					File db = new File(Webstats.getConfiguration().getString("database.database"));
					if (!db.isAbsolute())
						db = new File(Webstats.getDirectory(), db.toString());
					connection = DriverManager.getConnection("jdbc:sqlite:" + db);
					break;
				case OTHER:
					Class.forName(Webstats.getConfiguration().getString("advanced.driver"));
					connection = DriverManager.getConnection(Webstats.getConfiguration().getString("advanced.url"));
					break;
				default:
					throw new ClassNotFoundException();
			}
			// Now set up the tables and data
			for (String sql : tables[driver])
				Database.update(sql);
			Database.update("TRUNCATE ws_materials");
			for (Material m : Material.values())
				Database.update("INSERT INTO ws_materials VALUES (" + m.getId() + ", '" + m + "')");
			return true;
		} catch (ClassNotFoundException e) {
			Webstats.severe("Unable to load database driver. Your configuration may be incorrect.");
		} catch (SQLException e) {
			Webstats.severe("Unable to connect to database. Your configuration may be incorrect.");
		}
		return false;
	}

	public static void disconnect() {
		try {
			connection.close();
		} catch (SQLException ignore) {
		}
	}

	public static Result query(String sql) {
		Webstats.debug(sql);
		try {
			Statement s = connection.createStatement();
			return new Result(s, s.executeQuery(sql));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int update(String sql) {
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
