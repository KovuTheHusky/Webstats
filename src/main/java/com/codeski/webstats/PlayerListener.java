package com.codeski.webstats;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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

import com.codeski.webstats.databases.Database;

public class PlayerListener implements Listener {
	private final Database database;
	private final HashMap<String, Double> fallen = new HashMap<String, Double>();
	private final HashMap<String, Boolean> jumping = new HashMap<String, Boolean>();

	public PlayerListener(Database database) {
		this.database = database;
	}

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
		database.playerEggThrown(event.getPlayer(), event.getNumHatches());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		database.playerJoined(event.getPlayer());
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
		Player uuid = event.getPlayer();
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
			database.distanceFallen(uuid, fallen.put(name, 0.0));
		// Other distances
		if (event.getPlayer().isInsideVehicle())
			switch (vehicle.getType()) {
				case MINECART:
					database.distanceByMinecart(uuid, xyz);
					break;
				case BOAT:
					database.distanceByBoat(uuid, xyz);
					break;
				case PIG:
					database.distanceByPig(uuid, xyz);
					break;
				case HORSE:
				case UNKNOWN: // TODO Remove this once horses are fixed
					database.distanceByHorse(uuid, xyz); // TODO Calculate horse movement if bukkit does not fix
					break;
				default:
			}
		else if (event.getPlayer().isFlying())
			database.distanceFlown(uuid, xyz);
		else if (block.isLiquid() && !block.getRelative(BlockFace.UP).isLiquid())
			database.distanceSwam(uuid, xyz);
		else if (block.isLiquid())
			database.distanceDove(uuid, xyz);
		else if (block.getType() == Material.LADDER || block.getType() == Material.VINE)
			database.distanceClimbed(uuid, y);
		else
			database.distanceWalked(uuid, xyz);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		database.playerQuit(event.getPlayer());
	}
}
