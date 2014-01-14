package com.codeski.webstats;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
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
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
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
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.PLAYER)
			return;
		EntityDamageEvent damage = event.getEntity().getLastDamageCause();
		if (!(damage instanceof EntityDamageByEntityEvent))
			database.deathByEnvironment(damage.getCause(), event.getEntityType());
		else
		{
			Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();
			Projectile projectile = null;
			if (damager instanceof Projectile) {
				projectile = (Projectile) damager;
				damager = projectile.getShooter();
			}
			Material hand = null;
			if (damager instanceof Player && damage.getCause() == DamageCause.ENTITY_ATTACK)
				hand = ((Player) damager).getItemInHand().getType();
			else if (damage.getCause() == DamageCause.ENTITY_ATTACK)
				hand = ((LivingEntity) damager).getEquipment().getItemInHand().getType();
			// Insert data at this point...
			if (damager instanceof Player && projectile != null)
				database.deathByPlayer(damage.getCause(), event.getEntityType(), (Player) damager, projectile);
			else if (damager instanceof Player && hand != null)
				database.deathByPlayer(damage.getCause(), event.getEntityType(), (Player) damager, hand);
			else if (damager instanceof Player)
				database.deathByPlayer(damage.getCause(), event.getEntityType(), (Player) damager);
			else if (projectile != null)
				database.deathByEntity(damage.getCause(), event.getEntityType(), damager.getType(), projectile);
			else if (hand != null)
				database.deathByEntity(damage.getCause(), event.getEntityType(), damager.getType(), hand);
			else
				database.deathByEntity(damage.getCause(), event.getEntityType(), damager.getType());
		}
		//
		// snips and snails
		//
		// EntityDamageEvent dmg = event.getEntity().getLastDamageCause();
		// if (dmg instanceof EntityDamageByEntityEvent) {
		// Entity killer = ((EntityDamageByEntityEvent) dmg).getDamager();
		// if (killer.equals(event.getEntity()))
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntityType() + " died from itself's " + dmg.getCause() + ".");
		// else if (killer instanceof Player)
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntityType() + " died from " + ((Player) killer).getName() + "'s " + dmg.getCause() + " using " + ((Player) killer).getItemInHand().getType() + ".");
		// else if (killer instanceof Projectile) {
		// Entity projectile = killer;
		// killer = ((Projectile) killer).getShooter();
		// if (killer instanceof Player)
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntityType() + " died from " + ((Player) killer).getName() + "'s " + dmg.getCause() + " using " + projectile.getType() + ".");
		// else
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntityType() + " died from " + killer.getType() + "'s " + dmg.getCause() + " using " + projectile.getType() + ".");
		// }
		// else
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntityType() + " died from " + killer.getType() + "'s " + dmg.getCause() + ".");
		// }
		// else
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntityType() + " died from " + dmg.getCause() + ".");
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (event.isCancelled() || event.getEntityType() != EntityType.PLAYER)
			return;
		database.playerArrowShot((Player) event.getEntity());
	}

	@EventHandler
	public void onPlayerAnimation(PlayerAnimationEvent event) {
	}

	@EventHandler
	public void onPlayerBedEnterEvent(PlayerBedEnterEvent event) {
		database.playerEnteredBed(event.getPlayer());
	}

	@EventHandler
	public void onPlayerBedLeaveEvent(PlayerBedLeaveEvent event) {
		database.playerLeftBed(event.getPlayer());
	}

	@EventHandler
	public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
		Webstats.info("PlayerBucketEmptyEvent: " + event.getPlayer().getName() + " " + event.getBucket() + " " + event.getBlockClicked().getX() + "," + event.getBlockClicked().getY() + "," + event.getBlockClicked().getZ());
	}

	@EventHandler
	public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {
		Webstats.info("PlayerBucketFillEvent: " + event.getPlayer().getName() + " " + event.getBlockClicked().getType() + " " + event.getBlockClicked().getX() + "," + event.getBlockClicked().getY() + "," + event.getBlockClicked().getZ());
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
	public void onPlayerDeath(PlayerDeathEvent event) {
		EntityDamageEvent damage = event.getEntity().getLastDamageCause();
		if (!(damage instanceof EntityDamageByEntityEvent))
			database.deathByEnvironment(damage.getCause(), event.getEntity());
		else
		{
			Entity damager = ((EntityDamageByEntityEvent) damage).getDamager();
			Projectile projectile = null;
			if (damager instanceof Projectile) {
				projectile = (Projectile) damager;
				damager = projectile.getShooter();
			}
			Material hand = null;
			if (damager instanceof Player && damage.getCause() == DamageCause.ENTITY_ATTACK)
				hand = ((Player) damager).getItemInHand().getType();
			else if (damage.getCause() == DamageCause.ENTITY_ATTACK)
				hand = ((LivingEntity) damager).getEquipment().getItemInHand().getType();
			// Insert data at this point...
			if (damager instanceof Player && projectile != null)
				database.deathByPlayer(damage.getCause(), event.getEntity(), (Player) damager, projectile);
			else if (damager instanceof Player && hand != null)
				database.deathByPlayer(damage.getCause(), event.getEntity(), (Player) damager, hand);
			else if (damager instanceof Player)
				database.deathByPlayer(damage.getCause(), event.getEntity(), (Player) damager);
			else if (projectile != null)
				database.deathByEntity(damage.getCause(), event.getEntity(), damager.getType(), projectile);
			else if (hand != null)
				database.deathByEntity(damage.getCause(), event.getEntity(), damager.getType(), hand);
			else
				database.deathByEntity(damage.getCause(), event.getEntity(), damager.getType());
		}
		//
		// snips and snails
		//
		// EntityDamageEvent dmg = event.getEntity().getLastDamageCause();
		// if (dmg instanceof EntityDamageByEntityEvent) {
		// Entity killer = ((EntityDamageByEntityEvent) dmg).getDamager();
		// if (killer.equals(event.getEntity()))
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntity().getName() + " died from itself's " + dmg.getCause() + ".");
		// else if (killer instanceof Player)
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntity().getName() + " died from " + ((Player) killer).getName() + "'s " + dmg.getCause() + " using " + ((Player) killer).getItemInHand().getType() + ".");
		// else if (killer instanceof Projectile) {
		// Entity projectile = killer;
		// killer = ((Projectile) killer).getShooter();
		// if (killer instanceof Player)
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntity().getName() + " died from " + ((Player) killer).getName() + "'s " + dmg.getCause() + " using " + projectile.getType() + ".");
		// else
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntity().getName() + " died from " + killer.getType() + "'s " + dmg.getCause() + " using " + projectile.getType() + ".");
		// }
		// else
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntity().getName() + " died from " + killer.getType() + "'s " + dmg.getCause() + ".");
		// }
		// else
		// Bukkit.broadcastMessage(ChatColor.GREEN + "" + event.getEntity().getName() + " died from " + dmg.getCause() + ".");
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		database.itemDropped(event.getPlayer(), event.getItemDrop().getType(), event.getItemDrop().getItemStack().getAmount());
	}

	@EventHandler
	public void onPlayerEggThrow(PlayerEggThrowEvent event) {
		database.playerEggThrown(event.getPlayer(), event.getNumHatches());
	}

	@EventHandler
	public void onPlayerFish(PlayerFishEvent event) {
		if (event.getState() == State.FISHING)
			database.playerFish(event.getPlayer(), false);
		else if (event.getState() == State.CAUGHT_FISH)
			database.playerFish(event.getPlayer(), true);
	}

	@EventHandler
	public void onPlayerItemBreak(PlayerItemBreakEvent event) {
		database.itemBroken(event.getPlayer(), event.getBrokenItem().getType());
	}

	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		database.itemUsed(event.getPlayer(), event.getItem().getType());
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
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		database.itemPickedUp(event.getPlayer(), event.getItem().getType(), event.getItem().getItemStack().getAmount());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		database.playerQuit(event.getPlayer());
	}
}
