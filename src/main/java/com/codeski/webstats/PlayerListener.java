package com.codeski.webstats;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerChannelEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
	private final HashMap<String, Double> fallen = new HashMap<String, Double>();
	private final HashMap<String, Boolean> jumping = new HashMap<String, Boolean>();

	@EventHandler
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
	}

	@EventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event) {
	}

	@EventHandler
	public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
		Webstats.debug("BedEnterEvent: " + event.getPlayer().getName() + " " + event.getBed().getX() + "," + event.getBed().getY() + "," + event.getBed().getZ());
	}

	@EventHandler
	public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
		Webstats.debug("BedLeaveEvent: " + event.getPlayer().getName() + " " + event.getBed().getX() + "," + event.getBed().getY() + "," + event.getBed().getZ());
	}

	@EventHandler
	public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
		Webstats.debug("PlayerBucketEmptyEvent: " + event.getPlayer().getName() + " " + event.getBucket().getId() + " " + event.getBlockClicked().getX() + "," + event.getBlockClicked().getY() + "," + event.getBlockClicked().getZ());
	}

	@EventHandler
	public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
		Webstats.debug("PlayerBucketFillEvent: " + event.getPlayer().getName() + " " + event.getBlockClicked().getType().toString() + " " + event.getBlockClicked().getX() + "," + event.getBlockClicked().getY() + "," + event.getBlockClicked().getZ());
	}

	@EventHandler
	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
	}

	@EventHandler
	public void onPlayerChannelEvent(PlayerChannelEvent event) {
	}

	@EventHandler
	public void onPlayerChatTabCompleteEvent(PlayerChatTabCompleteEvent event) {
	}

	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Webstats.debug("PlayerDeathEvent: " + event.getEntity().getName() + " " + event.getEntity().getLocation().getX() + "," + event.getEntity().getLocation().getY() + "," + event.getEntity().getLocation().getZ());
	}

	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		Webstats.debug("PlayerDropItemEvent: " + event.getPlayer().getName() + " " + event.getItemDrop() + " " + event.getItemDrop().getLocation().getX() + "," + event.getItemDrop().getLocation().getY() + "," + event.getItemDrop().getLocation().getZ());
	}

	@EventHandler
	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
		Database.update("DROP TABLE IF EXISTS ws_blocks");
		Database.update("DROP TABLE IF EXISTS ws_damages");
		Database.update("DROP TABLE IF EXISTS ws_distances");
		Database.update("DROP TABLE IF EXISTS ws_eggs");
		Database.update("DROP TABLE IF EXISTS ws_fishes");
		Database.update("DROP TABLE IF EXISTS ws_items");
		Database.update("DROP TABLE IF EXISTS ws_materials");
		Database.update("DROP TABLE IF EXISTS ws_players");
		Database.update("DROP TABLE IF EXISTS ws_statistics");
		Database.disconnect();
		Database.connect();
		Webstats.info("Dropped database tables.");
		Webstats.broadcast("Dropped database tables.");
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		String name = event.getPlayer().getName();
		Result r = null;
		try {
			(r = Database.query("SELECT player_id FROM ws_players WHERE player_name = '" + name + "' LIMIT 1")).getResult().next();
			UUID old = UUID.fromString(r.getResult().getString("player_id"));
			if (!uuid.equals(old))
				Database.update("UPDATE ws_players LEFT JOIN ws_blocks ON player_id = block_player LEFT JOIN ws_distances ON player_id = distance_player LEFT JOIN ws_items ON player_id = item_player SET player_id = '" + uuid + "' WHERE player_name = '" + name + "'");
		} catch (SQLException ignore) {
		} finally {
			r.close();
		}
		int count = Database.update("UPDATE ws_players SET player_joins = player_joins + 1 WHERE player_id = '" + uuid + "'");
		if (count < 1)
			Database.update("INSERT INTO ws_players VALUES ('" + uuid + "', '" + name + "', 0, 0, 0, 0, 0, " + System.currentTimeMillis() / 1000 + ", 1, 0, 0, 0, 0, 0, 0, 0, 0, 0)");
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;
		Location from = event.getFrom();
		Location to = event.getTo();
		double xyz = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
		if (xyz == 0.0) {
			Webstats.info("No movement... skipping! v5");
			return;
		}
		// double xz = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
		double sy = to.getY() - from.getY();
		double y = Math.abs(sy);
		double fall = event.getPlayer().getFallDistance();
		UUID uuid = event.getPlayer().getUniqueId();
		String name = event.getPlayer().getName();
		Block block = event.getPlayer().getLocation().getBlock();
		Entity vehicle = event.getPlayer().getVehicle();
		if (vehicle != null)
			fall += vehicle.getFallDistance();
		if (this.fallen.get(name) == null)
			this.fallen.put(name, 0.0);
		if (this.jumping.get(name) == null)
			this.jumping.put(name, false);
		// Check for jump
		Block below = block.getRelative(BlockFace.DOWN);
		if (!this.jumping.get(name) && sy > 0 && !block.isLiquid() && !below.getType().isSolid() && !below.isLiquid() && below.getType() != Material.LADDER && below.getType() != Material.VINE)
			this.jumping.put(name, true);
		else if (this.jumping.get(name) && sy <= 0)
			this.jumping.put(name, false);
		// Distance fallen
		if (fall >= 2.0 && fall > this.fallen.get(name))
			this.fallen.put(name, fall);
		else if (fall == 0.0 && this.fallen.get(name) > 0.0) {
			if (Database.update("UPDATE ws_distances SET distance_fallen = distance_fallen + " + this.fallen.get(name) + " WHERE distance_player = '" + uuid + "'") < 1)
				Database.update("INSERT INTO ws_distances VALUES(0, 0, " + this.fallen.get(name) + ", 0, 0, 0, 0, 0, 0, 0, '" + uuid + "')");
			this.fallen.put(name, 0.0);
		}
		// Distance by vehicle
		if (event.getPlayer().isInsideVehicle())
			switch (vehicle.getType()) {
				case MINECART:
					if (Database.update("UPDATE ws_distances SET distance_by_minecart = distance_by_minecart + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
						Database.update("INSERT INTO ws_distances VALUES(0, 0, 0, 0, 0, 0, " + xyz + ", 0, 0, 0, '" + uuid + "')");
					break;
				case BOAT:
					if (Database.update("UPDATE ws_distances SET distance_by_boat = distance_by_boat + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
						Database.update("INSERT INTO ws_distances VALUES(0, 0, 0, 0, 0, 0, 0, " + xyz + ", 0, 0, '" + uuid + "')");
					break;
				case PIG:
					if (Database.update("UPDATE ws_distances SET distance_by_pig = distance_by_pig + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
						Database.update("INSERT INTO ws_distances VALUES(0, 0, 0, 0, 0, 0, 0, 0, " + xyz + ", 0, '" + uuid + "')");
					break;
				case GIANT: // TODO Change giant to horse for 1.6
					if (Database.update("UPDATE ws_distances SET distance_by_horse = distance_by_horse + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
						Database.update("INSERT INTO ws_distances VALUES(0, 0, 0, 0, 0, 0, 0, 0, 0, " + xyz + ", '" + uuid + "')");
					break;
				default:
			}
		// Distance flown
		else if (event.getPlayer().isFlying()) {
			if (Database.update("UPDATE ws_distances SET distance_flown = distance_flown + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
				Database.update("INSERT INTO ws_distances VALUES(0, 0, 0, 0, " + xyz + ", 0, 0, 0, 0, 0, '" + uuid + "')");
		}
		// Distance swam
		else if (block.isLiquid() && !block.getRelative(BlockFace.UP).isLiquid()) {
			if (Database.update("UPDATE ws_distances SET distance_swam = distance_swam + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
				Database.update("INSERT INTO ws_distances VALUES(0, " + xyz + ", 0, 0, 0, 0, 0, 0, 0, 0, '" + uuid + "')");
		}
		// Distance dove
		else if (block.isLiquid()) {
			if (Database.update("UPDATE ws_distances SET distance_dove = distance_dove + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
				Database.update("INSERT INTO ws_distances VALUES(0, 0, 0, 0, 0, " + xyz + ", 0, 0, 0, 0, '" + uuid + "')");
		}
		// Distance climbed
		else if (block.getType() == Material.LADDER || block.getType() == Material.VINE) {
			if (Database.update("UPDATE ws_distances SET distance_climbed = distance_climbed + " + y + " WHERE distance_player = '" + uuid + "'") < 1)
				Database.update("INSERT INTO ws_distances VALUES(0, 0, 0, " + y + ", 0, 0, 0, 0, 0, 0, '" + uuid + "')");
		} else if (Database.update("UPDATE ws_distances SET distance_walked = distance_walked + " + xyz + " WHERE distance_player = '" + uuid + "'") < 1)
			Database.update("INSERT INTO ws_distances VALUES(" + xyz + ", 0, 0, 0, 0, 0, 0, 0, 0, 0, '" + uuid + "')");
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		Database.update("UPDATE ws_players SET player_quits = player_quits + 1 WHERE player_id = '" + uuid + "'");
	}
}
