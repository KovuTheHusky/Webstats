package com.codeski.webstats.databases;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.codeski.webstats.Webstats;

public class SQLITEDatabase extends Database {
	private final String[] tables = {};

	@Override
	public void blockBroken(Player player, Material type) {
		// TODO Auto-generated method stub
	}

	@Override
	public void blockCrafted(HumanEntity player, Material type, int quantity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void blockPlaced(Player player, Material type) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean connect(FileConfiguration configuration) {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + configuration.getString("database.database"));
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
		// TODO Auto-generated method stub
	}

	@Override
	public void deathByEnvironment(DamageCause type, Player damagee) {
		// TODO Auto-generated method stub
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
	public void itemBroken(Player player, Material type) {
		// TODO Auto-generated method stub
	}

	@Override
	public void itemCrafted(HumanEntity player, Material type, int quantity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void itemDropped(Player player, EntityType type, int quantity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void itemPickedUp(Player player, EntityType type, int quantity) {
		// TODO Auto-generated method stub
	}

	@Override
	public void itemUsed(Player player, Material type) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerArrowShot(Player player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerEggThrown(Player player, int spawn) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerEnteredBed(Player player) {
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
	public void playerLeftBed(Player player) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerQuit(Player player) {
		// TODO Auto-generated method stub
	}
}
