package com.codeski.webstats.databases;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.codeski.webstats.Webstats;

public class MYSQLDatabase extends Database {
	private final String[] tables = {
			"CREATE TABLE IF NOT EXISTS ws_blocks (block_id INTEGER DEFAULT 0, block_broken INTEGER DEFAULT 0, block_crafted INTEGER DEFAULT 0, block_placed INTEGER DEFAULT 0, block_player BIGINT)",
			// "CREATE TABLE IF NOT EXISTS ws_damages (damage_type INTEGER, damage_damager_type INTEGER, damage_damager_id BIGINT, damage_damaged_type INTEGER, damage_damaged_id BIGINT)",
			"CREATE TABLE IF NOT EXISTS ws_distances (distance_walked DOUBLE DEFAULT 0 DEFAULT 0, distance_swam DOUBLE DEFAULT 0, distance_fallen DOUBLE DEFAULT 0, distance_climbed DOUBLE DEFAULT 0, distance_flown DOUBLE DEFAULT 0, distance_dove DOUBLE DEFAULT 0, distance_by_minecart DOUBLE DEFAULT 0, distance_by_boat DOUBLE DEFAULT 0, distance_by_pig DOUBLE DEFAULT 0, distance_by_horse DOUBLE DEFAULT 0, distance_player BIGINT PRIMARY KEY)",
			"CREATE TABLE IF NOT EXISTS ws_eggs (egg_thrown INTEGER DEFAULT 0, egg_hatch INTEGER DEFAULT 0, egg_spawn INTEGER DEFAULT 0, egg_player BIGINT PRIMARY KEY)",
			"CREATE TABLE IF NOT EXISTS ws_entities (entity_id INTEGER PRIMARY KEY AUTO_INCREMENT, entity_name CHAR(32) UNIQUE)",
			"CREATE TABLE IF NOT EXISTS ws_fishes (fish_casts INTEGER DEFAULT 0, fish_catches INTEGER DEFAULT 0, fish_player BIGINT PRIMARY KEY)",
			"CREATE TABLE IF NOT EXISTS ws_items (item_id INTEGER DEFAULT 0, item_broken INTEGER DEFAULT 0, item_crafted INTEGER DEFAULT 0, item_dropped INTEGER DEFAULT 0, item_picked_up INTEGER DEFAULT 0, item_used INTEGER DEFAULT 0, item_player BIGINT)",
			"CREATE TABLE IF NOT EXISTS ws_damages (damage_id INTEGER PRIMARY KEY AUTO_INCREMENT, damage_name CHAR(32) UNIQUE)",
			"CREATE TABLE IF NOT EXISTS ws_deaths (death_id INTEGER PRIMARY KEY AUTO_INCREMENT, death_count INTEGER DEFAULT 0, death_type INTEGER NOT NULL, death_damagee_type INTEGER NOT NULL, death_damagee_player BIGINT, death_damager_type INTEGER, death_damager_player BIGINT, death_projectile INTEGER, death_hand INTEGER)",
			"CREATE TABLE IF NOT EXISTS ws_materials (material_id INTEGER PRIMARY KEY AUTO_INCREMENT, material_name CHAR(32) UNIQUE)",
			"CREATE TABLE IF NOT EXISTS ws_players (player_id BIGINT PRIMARY KEY AUTO_INCREMENT, player_name CHAR(16) UNIQUE, player_uuid CHAR(36) UNIQUE, player_arrows INTEGER DEFAULT 0, player_beds_entered INTEGER DEFAULT 0, player_beds_left INTEGER DEFAULT 0, player_commands INTEGER DEFAULT 0, player_ender INTEGER, player_first INTEGER, player_joined INTEGER DEFAULT 0, player_jumps INTEGER DEFAULT 0, player_kicks INTEGER DEFAULT 0, player_last INTEGER, player_longest INTEGER DEFAULT 0, player_online INTEGER DEFAULT 0, player_played INTEGER DEFAULT 0, player_portals INTEGER DEFAULT 0, player_quit INTEGER DEFAULT 0, player_words INTEGER DEFAULT 0)",
			"CREATE TABLE IF NOT EXISTS ws_statistics (statistic_first INTEGER, statistic_startup INTEGER, statistic_shutdown INTEGER, statistic_uptime INTEGER DEFAULT 0, statistic_players INTEGER, statistic_peak INTEGER DEFAULT 0)"
	};
	private final Timer timer = new Timer();

	@Override
	public void blockBroken(Player player, Material type) {
		int count = this.update("UPDATE ws_blocks JOIN ws_materials ON block_id = material_id JOIN ws_players ON block_player = player_id SET block_broken = block_broken + 1 WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_blocks (block_id, block_broken, block_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), 1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void blockCrafted(HumanEntity player, Material type, int quantity) {
		int count = this.update("UPDATE ws_blocks JOIN ws_materials ON block_id = material_id JOIN ws_players ON block_player = player_id SET block_crafted = block_crafted + " + quantity + " WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_blocks (block_id, block_crafted, block_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), " + quantity + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void blockPlaced(Player player, Material type) {
		int count = this.update("UPDATE ws_blocks JOIN ws_materials ON block_id = material_id JOIN ws_players ON block_player = player_id SET block_placed = block_placed + 1 WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_blocks (block_id, block_placed, block_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), 1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
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
		ArrayList<DamageCause> currentDamages = new ArrayList<DamageCause>();
		Result damages = this.query("SELECT damage_name FROM ws_damages ORDER BY damage_id ASC");
		try {
			while (damages.getResult().next())
				currentDamages.add(DamageCause.valueOf(damages.getResult().getString(1)));
			damages.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (DamageCause d : DamageCause.values())
			if (!currentDamages.contains(d))
				this.update("INSERT INTO ws_damages VALUES (NULL, '" + d + "')");
		ArrayList<EntityType> currentEntities = new ArrayList<EntityType>();
		Result entities = this.query("SELECT entity_name FROM ws_entities ORDER BY entity_id ASC");
		try {
			while (entities.getResult().next())
				currentEntities.add(EntityType.valueOf(entities.getResult().getString(1)));
			entities.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (EntityType e : EntityType.values())
			if (!currentEntities.contains(e))
				this.update("INSERT INTO ws_entities VALUES (NULL, '" + e + "')");
		ArrayList<Material> currentMaterials = new ArrayList<Material>();
		Result materials = this.query("SELECT material_name FROM ws_materials ORDER BY material_id ASC");
		try {
			while (materials.getResult().next())
				currentMaterials.add(Material.valueOf(materials.getResult().getString(1)));
			materials.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (Material m : Material.values())
			if (!currentMaterials.contains(m))
				this.update("INSERT INTO ws_materials VALUES (NULL, '" + m + "')");
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
					}
				}
			}
		}, 1000, 1000);
		for (Player p : Webstats.getOnlinePlayers())
			this.playerJoined(p);
		return true;
	}

	@Override
	public void deathByEntity(DamageCause type, EntityType damagee, EntityType damager) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByEntity(DamageCause type, EntityType damagee, EntityType damager, Material hand) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByEntity(DamageCause type, EntityType damagee, EntityType damager, Projectile projectile) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByEntity(DamageCause type, Player damagee, EntityType damager) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByEntity(DamageCause type, Player damagee, EntityType damager, Material hand) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByEntity(DamageCause type, Player damagee, EntityType damager, Projectile projectile) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByEnvironment(DamageCause type, EntityType damagee) {
		Bukkit.broadcastMessage(type + " killed " + damagee + ".");
		int count = this.update("UPDATE ws_deaths JOIN ws_damages ON death_type = damage_id JOIN ws_entities ON death_damagee_type = entity_id SET death_count = death_count + 1 WHERE damage_name = '" + type + "' AND entity_name = '" + damagee + "'");
		if (count < 1)
			this.update("INSERT INTO ws_deaths (death_id, death_count, death_type, death_damagee_type) VALUES (NULL, 1, (SELECT damage_id FROM ws_damages WHERE damage_name = '" + type + "'), (SELECT entity_id FROM ws_entities WHERE entity_name = '" + damagee + "'))");
	}

	@Override
	public void deathByEnvironment(DamageCause type, Player damagee) {
		Bukkit.broadcastMessage(type + " killed " + damagee + ".");
		int count = this.update("UPDATE ws_deaths JOIN ws_damages ON death_type = damage_id JOIN ws_entities ON death_damagee_type = entity_id JOIN ws_players ON death_damagee_player = player_id SET death_count = death_count + 1 WHERE damage_name = '" + type + "' AND entity_name = '" + damagee.getType() + "' AND player_name = '" + damagee.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_deaths (death_id, death_count, death_type, death_damagee_type, death_damagee_player) VALUES (NULL, 1, (SELECT damage_id FROM ws_damages WHERE damage_name = '" + type + "'), (SELECT entity_id FROM ws_entities WHERE entity_name = '" + damagee.getType() + "'), (SELECT player_id FROM ws_players WHERE player_name = '" + damagee.getName() + "'))");
	}

	@Override
	public void deathByPlayer(DamageCause type, EntityType damagee, Player damager) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByPlayer(DamageCause type, EntityType damagee, Player damager, Material hand) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByPlayer(DamageCause type, EntityType damagee, Player damager, Projectile projectile) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByPlayer(DamageCause type, Player damagee, Player damager) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByPlayer(DamageCause type, Player damagee, Player damager, Material hand) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByPlayer(DamageCause type, Player damagee, Player damager, Projectile projectile) {
		// TODO Auto-generated method stub
	}

	@Override
	public void disconnect() {
		for (Player p : Webstats.getOnlinePlayers())
			this.playerQuit(p);
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
	public void itemBroken(Player player, Material type) {
		int count = this.update("UPDATE ws_items JOIN ws_materials ON item_id = material_id JOIN ws_players ON item_player = player_id SET item_broken = item_broken + 1 WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_items (item_id, item_broken, item_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), 1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void itemCrafted(HumanEntity player, Material type, int quantity) {
		int count = this.update("UPDATE ws_items JOIN ws_materials ON item_id = material_id JOIN ws_players ON item_player = player_id SET item_crafted = item_crafted + " + quantity + " WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_items (item_id, item_crafted, item_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), " + quantity + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void itemDropped(Player player, EntityType type, int quantity) {
		int count = this.update("UPDATE ws_items JOIN ws_materials ON item_id = material_id JOIN ws_players ON item_player = player_id SET item_dropped = item_dropped + " + quantity + " WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_items (item_id, item_dropped, item_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), " + quantity + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void itemPickedUp(Player player, EntityType type, int quantity) {
		int count = this.update("UPDATE ws_items JOIN ws_materials ON item_id = material_id JOIN ws_players ON item_player = player_id SET item_picked_up = item_picked_up + " + quantity + " WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_items (item_id, item_picked_up, item_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), " + quantity + ", (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void itemUsed(Player player, Material type) {
		int count = this.update("UPDATE ws_items JOIN ws_materials ON item_id = material_id JOIN ws_players ON item_player = player_id SET item_used = item_used + 1 WHERE material_name = '" + type + "' AND player_name = '" + player.getName() + "'");
		if (count < 1)
			this.update("INSERT INTO ws_items (item_id, item_used, item_player) VALUES ((SELECT material_id FROM ws_materials WHERE material_name = '" + type + "'), 1, (SELECT player_id FROM ws_players WHERE player_name = '" + player.getName() + "'))");
	}

	@Override
	public void playerArrowShot(Player player) {
		this.update("UPDATE ws_players SET player_arrows = player_arrows + 1 WHERE player_name = '" + player.getName() + "'");
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
	public void playerEnteredBed(Player player) {
		this.update("UPDATE ws_players SET player_beds_entered = player_beds_entered + 1 WHERE player_name = '" + player.getName() + "'");
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
		try {
			Result r = this.query("SELECT statistic_peak FROM ws_statistics");
			r.getResult().next();
			if (Webstats.getOnlinePlayers().length > r.getResult().getInt(1))
				this.update("UPDATE ws_statistics SET statistic_peak = " + Webstats.getOnlinePlayers().length);
			r.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void playerLeftBed(Player player) {
		this.update("UPDATE ws_players SET player_beds_left = player_beds_left + 1 WHERE player_name = '" + player.getName() + "'");
	}

	@Override
	public void playerQuit(Player player) {
		this.update("UPDATE ws_players SET player_quit = player_quit + 1, player_online = 0 WHERE player_name = '" + player.getName() + "'");
	}
}
