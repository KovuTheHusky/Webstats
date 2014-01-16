package com.codeski.webstats.databases;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.codeski.webstats.MaterialEvent;
import com.codeski.webstats.Webstats;

public class MYSQLDatabase extends Database {
	private final String[] tables = {
			"" };

	@Override
	public boolean connect(FileConfiguration configuration) {
		// Start debug timer if applicable
		if (Webstats.debugging || configuration.getBoolean("developer.debugging"))
			this.debugTimer();
		// Connect to the database
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
		// Run the table creation script
		for (String sql : tables)
			this.update(sql);
		// Damage types
		ArrayList<DamageCause> currentDamages = new ArrayList<DamageCause>();
		Result damages = this.query("SELECT type_name FROM ws_damage_types ORDER BY type_id ASC");
		try {
			while (damages.getResult().next())
				currentDamages.add(DamageCause.valueOf(damages.getResult().getString(1)));
			damages.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (DamageCause d : DamageCause.values())
			if (!currentDamages.contains(d))
				this.update("INSERT INTO ws_damage_types VALUES (NULL, '" + d + "')");
		// Entity types
		ArrayList<EntityType> currentEntities = new ArrayList<EntityType>();
		Result entities = this.query("SELECT type_name FROM ws_entity_types ORDER BY type_id ASC");
		try {
			while (entities.getResult().next())
				currentEntities.add(EntityType.valueOf(entities.getResult().getString(1)));
			entities.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (EntityType e : EntityType.values())
			if (!currentEntities.contains(e))
				this.update("INSERT INTO ws_entity_types VALUES (NULL, '" + e + "')");
		// Material types
		ArrayList<Material> currentMaterials = new ArrayList<Material>();
		Result materials = this.query("SELECT type_name FROM ws_material_types ORDER BY type_id ASC");
		try {
			while (materials.getResult().next())
				currentMaterials.add(Material.valueOf(materials.getResult().getString(1)));
			materials.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (Material m : Material.values())
			if (!currentMaterials.contains(m))
				this.update("INSERT INTO ws_material_types VALUES (NULL, '" + m + "', " + m.isBlock() + ")");
		// Material events
		ArrayList<MaterialEvent> currentMaterialEvents = new ArrayList<MaterialEvent>();
		Result materialEvents = this.query("SELECT event_name FROM ws_material_events ORDER BY event_id ASC");
		try {
			while (materialEvents.getResult().next())
				currentMaterialEvents.add(MaterialEvent.valueOf(materialEvents.getResult().getString(1)));
			materialEvents.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (MaterialEvent m : MaterialEvent.values())
			if (!currentMaterialEvents.contains(m))
				this.update("INSERT INTO ws_material_events VALUES (NULL, '" + m + "')");
		// World types
		ArrayList<WorldType> currentWorldTypes = new ArrayList<WorldType>();
		Result worldTypes = this.query("SELECT type_name FROM ws_world_types ORDER BY type_id ASC");
		try {
			while (worldTypes.getResult().next())
				currentWorldTypes.add(WorldType.valueOf(worldTypes.getResult().getString(1)));
			worldTypes.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (WorldType w : WorldType.values())
			if (!currentWorldTypes.contains(w))
				this.update("INSERT INTO ws_world_types VALUES (NULL, '" + w + "')");
		// World environments
		ArrayList<Environment> currentworldEnvironments = new ArrayList<Environment>();
		Result worldEnvironments = this.query("SELECT environment_name FROM ws_world_environments ORDER BY environment_id ASC");
		try {
			while (worldEnvironments.getResult().next())
				currentworldEnvironments.add(Environment.valueOf(worldEnvironments.getResult().getString(1)));
			worldEnvironments.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (Environment w : Environment.values())
			if (!currentworldEnvironments.contains(w))
				this.update("INSERT INTO ws_world_environments VALUES (NULL, '" + w + "')");
		// List of worlds
		ArrayList<World> currentWorlds = new ArrayList<World>();
		Result worlds = this.query("SELECT world_name FROM ws_worlds ORDER BY world_id ASC");
		try {
			while (worlds.getResult().next())
				currentWorlds.add(Bukkit.getWorld(worlds.getResult().getString(1)));
			worlds.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		for (World w : Bukkit.getWorlds())
			if (!currentWorlds.contains(w))
				this.update("INSERT INTO ws_worlds VALUES (NULL, '" + w.getName() + "', '" + w.getUID() + "', (SELECT type_id FROM ws_world_types WHERE type_name = '" + w.getWorldType() + "'), (SELECT environment_id FROM ws_world_environments WHERE environment_name = '" + w.getEnvironment() + "'))");
		for (Player p : Bukkit.getOnlinePlayers())
			this.playerJoined(p.getName(), p.getUniqueId() + "");
		return true;
	}

	@Override
	public void disconnect() {
		for (Player p : Bukkit.getOnlinePlayers())
			this.playerQuit(p);
		long time = System.currentTimeMillis() / 1000;
		this.update_old("UPDATE ws_statistics SET statistic_shutdown = " + time);
		try {
			connection.close();
		} catch (SQLException ignore) {
		}
	}

	@Override
	public void materialEvent(String player, String event, String type, String count, String world, String x, String y, String z) {
		if (player != null)
			this.update("INSERT INTO ws_materials VALUES (DEFAULT, (SELECT player_id FROM ws_players WHERE player_name = '" + player + "'), (SELECT event_id FROM ws_material_events WHERE event_name = '" + event + "'), (SELECT type_id FROM ws_material_types WHERE type_name = '" + type + "'), " + count + ", DEFAULT, (SELECT world_id FROM ws_worlds WHERE world_name = '" + world + "'), " + x + ", " + y + ", " + z + ")");
		else
			this.update("INSERT INTO ws_materials VALUES (DEFAULT, NULL, (SELECT event_id FROM ws_material_events WHERE event_name = '" + event + "'), (SELECT type_id FROM ws_material_types WHERE type_name = '" + type + "'), " + count + ", DEFAULT, (SELECT world_id FROM ws_worlds WHERE world_name = '" + world + "'), " + x + ", " + y + ", " + z + ")");
	}

	@Override
	public void playerJoined(String player, String uuid) {
		this.update("INSERT IGNORE INTO ws_players (player_name, player_uuid) VALUES ('" + player + "', '" + uuid + "')");
		this.update("INSERT INTO ws_statistics VALUES (NULL, 'peak', " + Bukkit.getOnlinePlayers().length + ") ON DUPLICATE KEY UPDATE statistic_value = IF(" + Bukkit.getOnlinePlayers().length + " > statistic_value, " + Bukkit.getOnlinePlayers().length + ", statistic_value)");
	}

	@Override
	public void playerQuit(Player player) {
		// TODO Auto-generated method stub
	}
}
