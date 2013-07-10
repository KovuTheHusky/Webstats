package com.codeski.webstats.databases;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.codeski.webstats.Webstats;

public class MYSQLDatabase extends Database {
	private final String[] tables = {
			"CREATE TABLE IF NOT EXISTS ws_blocks (block_id INTEGER DEFAULT 0, block_data INTEGER DEFAULT 0, block_broken INTEGER DEFAULT 0, block_crafted INTEGER DEFAULT 0, block_placed INTEGER DEFAULT 0, block_player BIGINT)",
			// "CREATE TABLE IF NOT EXISTS ws_damages (damage_type INTEGER, damage_damager_type INTEGER, damage_damager_id BIGINT, damage_damaged_type INTEGER, damage_damaged_id BIGINT)",
			"CREATE TABLE IF NOT EXISTS ws_distances (distance_walked DOUBLE DEFAULT 0 DEFAULT 0, distance_swam DOUBLE DEFAULT 0, distance_fallen DOUBLE DEFAULT 0, distance_climbed DOUBLE DEFAULT 0, distance_flown DOUBLE DEFAULT 0, distance_dove DOUBLE DEFAULT 0, distance_by_minecart DOUBLE DEFAULT 0, distance_by_boat DOUBLE DEFAULT 0, distance_by_pig DOUBLE DEFAULT 0, distance_by_horse DOUBLE DEFAULT 0, distance_player BIGINT PRIMARY KEY)",
			"CREATE TABLE IF NOT EXISTS ws_eggs (egg_thrown INTEGER DEFAULT 0, egg_hatch INTEGER DEFAULT 0, egg_spawn INTEGER DEFAULT 0, egg_player BIGINT PRIMARY KEY)",
			"CREATE TABLE IF NOT EXISTS ws_fishes (fish_casts INTEGER DEFAULT 0, fish_catches INTEGER DEFAULT 0, fish_player BIGINT PRIMARY KEY)",
			// "CREATE TABLE IF NOT EXISTS ws_items (item_id INTEGER, item_broken INTEGER, item_crafted INTEGER, item_dropped INTEGER, item_picked_up INTEGER, item_used INTEGER, item_player BIGINT)",
			"CREATE TABLE IF NOT EXISTS ws_materials (material_id INTEGER PRIMARY KEY, material_name CHAR(32))",
			"CREATE TABLE IF NOT EXISTS ws_players (player_id BIGINT PRIMARY KEY AUTO_INCREMENT, player_name CHAR(16) UNIQUE, player_uuid CHAR(36) UNIQUE, player_arrows INTEGER DEFAULT 0, player_beds_entered INTEGER DEFAULT 0, player_beds_left INTEGER DEFAULT 0, player_commands INTEGER DEFAULT 0, player_ender INTEGER, player_first INTEGER, player_joined INTEGER DEFAULT 0, player_jumps INTEGER DEFAULT 0, player_kicks INTEGER DEFAULT 0, player_last INTEGER, player_longest INTEGER DEFAULT 0, player_online INTEGER DEFAULT 0, player_played INTEGER DEFAULT 0, player_portals INTEGER DEFAULT 0, player_quit INTEGER DEFAULT 0, player_words INTEGER DEFAULT 0)",
			"CREATE TABLE IF NOT EXISTS ws_statistics (statistic_first INTEGER, statistic_startup INTEGER, statistic_shutdown INTEGER, statistic_uptime INTEGER DEFAULT 0, statistic_players INTEGER)"
	};
	private final Timer timer = new Timer();

	@Override
	public void blockBroken(Player player, int block, byte data) {
		int count = this.update("UPDATE ws_blocks JOIN ws_players ON block_player = player_id SET block_broken = block_broken + 1 WHERE block_id = " + block + " AND block_data = " + data + " AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_blocks (block_id, block_data, block_broken, block_player) VALUES (" + block + ", " + data + ", 1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void blockPlaced(Player player, int block, byte data) {
		int count = this.update("UPDATE ws_blocks JOIN ws_players ON block_player = player_id SET block_placed = block_placed + 1 WHERE block_id = " + block + " AND block_data = " + data + " AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_blocks (block_id, block_data, block_placed, block_player) VALUES (" + block + ", " + data + ", 1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public boolean connect(FileConfiguration configuration) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + configuration.getString("database.host") + "/" + configuration.getString("database.database") + "?user=" + configuration.getString("database.user") + "&password=" + configuration.getString("database.pass"));
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
		long time = System.currentTimeMillis() / 1000;
		int players = Webstats.getMaxPlayers();
		int count = this.update("UPDATE ws_statistics SET statistic_startup = " + time + ", statistic_players = " + players);
		if (count < 1)
			this.update("INSERT INTO ws_statistics (statistic_startup, statistic_first, statistic_players) VALUES (" + time + ", " + time + ", " + players + ")");
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				Webstats.debug("UPDATE ws_statistics SET statistic_uptime = statistic_uptime + 1");
				Statement s = null;
				try {
					s = connection.createStatement();
					s.executeUpdate("UPDATE ws_statistics SET statistic_uptime = statistic_uptime + 1");
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					try {
						s.close();
					} catch (SQLException ignore) {
					} catch (NullPointerException ignore) {
					}
				}
			}
		}, 1000, 1000);
		return true;
	}

	@Override
	public void disconnect() {
		timer.cancel();
		long time = System.currentTimeMillis() / 1000;
		this.update("UPDATE ws_statistics SET statistic_shutdown = " + time);
		try {
			connection.close();
		} catch (SQLException ignore) {
		}
	}

	@Override
	public void distanceByBoat(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_by_boat = distance_by_boat + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_by_boat, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceByHorse(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_by_horse = distance_by_horse + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_by_horse, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceByMinecart(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_by_minecart = distance_by_minecart + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_by_minecart, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceByPig(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_by_pig = distance_by_pig + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_by_pig, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceClimbed(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_climbed = distance_climbed + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_climbed, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceDove(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_dove = distance_dove + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_dove, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceFallen(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_fallen = distance_fallen + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_fallen, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceFlown(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_flown = distance_flown + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_flown, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceSwam(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_swam = distance_swam + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_swam, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void distanceWalked(Player player, double distance) {
		int count = this.update("UPDATE ws_distances JOIN ws_players ON distance_player = player_id SET distance_walked = distance_walked + " + distance + " WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_distances (distance_walked, distance_player) VALUES (" + distance + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void playerEggThrown(Player player, int spawn) {
		if (spawn > 0) {
			int count = this.update("UPDATE ws_eggs JOIN ws_players ON egg_player = player_id SET egg_thrown = egg_thrown + 1, egg_hatch = egg_hatch + 1, egg_spawn = egg_spawn + " + spawn + " WHERE player_name = '" + player.getName() + "'");
			if (count < 1)
				this.update("INSERT INTO ws_eggs (egg_thrown, egg_hatch, egg_spawn, egg_player) VALUES (1, 1, " + spawn + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
		} else {
			int count = this.update("UPDATE ws_eggs JOIN ws_players ON egg_player = player_id SET egg_thrown = egg_thrown + 1 WHERE player_name = '" + player.getName() + "'");
			if (count < 1)
				this.update("INSERT INTO ws_eggs (egg_thrown, egg_player) VALUES (1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
		}
	}

	@Override
	public void playerFish(Player player, boolean caught) {
		if (caught) {
			int count = this.update("UPDATE ws_fishes JOIN ws_players ON fish_player = player_id SET fish_catches = fish_catches + 1 WHERE player_name = '" + player.getName() + "'");
			if (count < 1)
				this.update("INSERT INTO ws_fishes (fish_catches, fish_player) VALUES (1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
		} else {
			int count = this.update("UPDATE ws_fishes JOIN ws_players ON fish_player = player_id SET fish_casts = fish_casts + 1 WHERE player_name = '" + player.getName() + "'");
			if (count < 1)
				this.update("INSERT INTO ws_fishes (fish_casts, fish_player) VALUES (1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
		}
	}

	@Override
	public void playerJoined(Player player) {
		int count = this.update("UPDATE ws_players SET player_joined = player_joined + 1, player_online = 1 WHERE player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_players (player_joined, player_online, player_name, player_uuid) VALUES (1, 1, '" + player.getName() + "', '" + player.getUniqueId() + "')");
	}

	@Override
	public void playerQuit(Player player) {
		this.update("UPDATE ws_players SET player_quit = player_quit + 1, player_online = 0 WHERE player_name = '" + player.getName() + "'");
	}
}
