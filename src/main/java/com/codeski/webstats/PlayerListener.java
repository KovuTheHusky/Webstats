package com.codeski.webstats;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.codeski.webstats.databases.Database;

public class PlayerListener implements Listener {
	private final Database database;
	private final HashMap<String, Double> fallen = new HashMap<String, Double>();
	private final HashMap<String, Boolean> jumping = new HashMap<String, Boolean>();

	public PlayerListener(Database database) {
		this.database = database;
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (event.isCancelled())
			return;
		Webstats.debug(event.getEntity() + "");
	}

	@EventHandler
	public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
		Webstats.debug("PlayerBucketEmptyEvent: " + event.getPlayer().getName() + " " + event.getBucket() + " " + event.getBlockClicked().getX() + "," + event.getBlockClicked().getY() + "," + event.getBlockClicked().getZ());
	}

	@EventHandler
	public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
		Webstats.debug("PlayerBucketFillEvent: " + event.getPlayer().getName() + " " + event.getBlockClicked().getType() + " " + event.getBlockClicked().getX() + "," + event.getBlockClicked().getY() + "," + event.getBlockClicked().getZ());
	}

	@EventHandler
	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
		Webstats.debug(event.getPlayer() + " " + event.getNumHatches());
	}

	@EventHandler
	public void onPlayerFish(PlayerFishEvent event) {
		if (event.getState() == State.FISHING)
			Webstats.debug(event.getPlayer() + " " + false);
		else if (event.getState() == State.CAUGHT_FISH)
			Webstats.debug(event.getPlayer() + " " + true);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		database.playerJoined(event.getPlayer().getName(), event.getPlayer().getUniqueId() + "");
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;
		Location from = event.getFrom();
		Location to = event.getTo();
		double xyz = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getY() - from.getY(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
		if (xyz == 0.0)
			return;
		// double xz = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
		double sy = to.getY() - from.getY();
		double y = Math.abs(sy);
		double fall = event.getPlayer().getFallDistance();
		String name = event.getPlayer().getName();
		Block block = event.getPlayer().getLocation().getBlock();
		Block below = block.getRelative(BlockFace.DOWN);
		Entity vehicle = event.getPlayer().getVehicle();
		if (vehicle != null)
			fall += vehicle.getFallDistance();
		if (fallen.get(name) == null)
			fallen.put(name, 0.0);
		if (jumping.get(name) == null)
			jumping.put(name, false);
		// Check for jump
		if (!jumping.get(name) && sy > 0 && !block.isLiquid() && !below.getType().isSolid() && !below.isLiquid() && below.getType() != Material.LADDER && below.getType() != Material.VINE)
			jumping.put(name, true);
		else if (jumping.get(name) && sy <= 0)
			jumping.put(name, false);
		// Distance fallen
		if (fall >= 2.0 && fall > fallen.get(name))
			fallen.put(name, fall);
		else if (fall == 0.0 && fallen.get(name) > 0.0)
			database.addDistance(name, Distance.FALLEN + "", fallen.put(name, 0.0) + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
		// Other distances
		if (event.getPlayer().isInsideVehicle())
			switch (vehicle.getType()) {
				case MINECART:
					database.addDistance(name, Distance.BY_MINECART + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
					break;
				case BOAT:
					database.addDistance(name, Distance.BY_BOAT + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
					break;
				case PIG:
					database.addDistance(name, Distance.BY_PIG + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
					break;
				case HORSE:
					database.addDistance(name, Distance.BY_HORSE + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
					break;
				default:
					Bukkit.getLogger().warning(vehicle.getType() + " is currently unsupported. Do you have the latest version of Webstats?");
			}
		else if (event.getPlayer().isFlying())
			database.addDistance(name, Distance.FLOWN + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
		else if (block.isLiquid() && !block.getRelative(BlockFace.UP).isLiquid())
			database.addDistance(name, Distance.SWAM + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
		else if (block.isLiquid())
			database.addDistance(name, Distance.DOVE + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
		else if (block.getType() == Material.LADDER || block.getType() == Material.VINE)
			database.addDistance(name, Distance.CLIMBED + "", y + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
		else
			database.addDistance(name, Distance.WALKED + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		database.playerQuit(event.getPlayer().getName());
	}
}
