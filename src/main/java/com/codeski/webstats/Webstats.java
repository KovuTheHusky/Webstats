package com.codeski.webstats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.codeski.webstats.databases.Database;
import com.codeski.webstats.databases.MYSQLDatabase;

public class Webstats extends JavaPlugin {
	private static FileConfiguration configuration;
	private static Database database;
	private static Logger logger;

	public static void debug(String msg) {
		if (configuration.getBoolean("developer.debugging"))
			logger.info(msg);
	}

	public static void info(String msg) {
		logger.info(msg);
	}

	public static void severe(String msg) {
		logger.severe(msg);
	}

	public static void warning(String msg) {
		logger.warning(msg);
	}

	private final HashMap<String, Double> fallen = new HashMap<String, Double>();
	private final HashMap<String, Boolean> jumping = new HashMap<String, Boolean>();

	@Override
	public void onDisable() {
		database.disconnect();
	}

	@Override
	public void onEnable() {
		logger = this.getLogger();
		// Figure out the configuration
		this.saveDefaultConfig();
		configuration = this.getConfig();
		configuration.options().copyDefaults(true);
		this.saveConfig();
		// Update extracted files if needed
		this.extractResources();
		// Get the database going
		if (!configuration.getBoolean("advanced.enabled")) {
			String d = configuration.getString("database.driver");
			if (d.equalsIgnoreCase("MYSQL"))
				database = new MYSQLDatabase();
			else if (d.equalsIgnoreCase("POSTGRESQL"))
				database = null;
			else if (d.equalsIgnoreCase("SQLITE"))
				database = null;
		} else
			database = null;
		if (!database.connect(configuration))
			return;
		// Start listening for events
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new Listener() {
			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event) {
				database.playerJoined(event.getPlayer().getName(), event.getPlayer().getUniqueId() + "");
			}

			@EventHandler
			public void onPlayerQuit(PlayerQuitEvent event) {
				database.playerQuit(event.getPlayer().getName());
			}
		}, this);
		// Block events
		if (configuration.getBoolean("events.block.break"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockBreak(BlockBreakEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.BROKEN + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.burn"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockBurn(BlockBurnEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BURNED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.damage"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockDamage(BlockDamageEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.DAMAGED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.dispense"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockDispense(BlockDispenseEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.DISPENSED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.experience"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockExp(BlockExpEvent event) {
					database.addMaterial(null, Material.Event.BLOCK_EXP + "", Material.getMaterial(event.getBlock()) + "", event.getExpToDrop() + "", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.fade"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockFade(BlockFadeEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.FADED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.fromto"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockFromTo(BlockFromToEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BLOCK_FROM_TO + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.grow"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockGrow(BlockGrowEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.GROWN + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.ignite"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockIgnite(BlockIgniteEvent event) {
					if (event.isCancelled())
						return;
					if (event.getPlayer() != null)
						database.addMaterial(event.getPlayer().getName(), Material.Event.IGNITED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
					else
						database.addMaterial(null, Material.Event.IGNITED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.physics"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockPhysics(BlockPhysicsEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BLOCK_PHYSICS + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.piston"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockPistonExtend(BlockPistonExtendEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BLOCK_PISTON_EXTEND + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}

				@EventHandler
				public void onBlockPistonRetract(BlockPistonRetractEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BLOCK_PISTON_RETRACT + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.place"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockPlace(BlockPlaceEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.PLACED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.redstone"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockRedstone(BlockRedstoneEvent event) {
					database.addMaterial(null, Material.Event.BLOCK_REDSTONE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.brew"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBrew(BrewEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BREW + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.furnace"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onFurnaceBurn(FurnaceBurnEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.FURNACE_BURN + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}

				@EventHandler
				public void onFurnaceSmelt(FurnaceSmeltEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.FURNACE_SMELT + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.decay"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onLeavesDecay(LeavesDecayEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.DECAYED + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.notes"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onNotePlay(NotePlayEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.NOTE_PLAY + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.signs"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onSignChange(SignChangeEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.SIGN_CHANGE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		// Item events
		if (configuration.getBoolean("events.item.break"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onPlayerItemBreak(PlayerItemBreakEvent event) {
					database.addMaterial(event.getPlayer().getName(), Material.Event.DEPLETED + "", Material.getMaterial(event.getBrokenItem()) + "", "1", event.getPlayer().getWorld().getName(), event.getPlayer().getLocation().getBlockX() + "", event.getPlayer().getLocation().getBlockY() + "", event.getPlayer().getLocation().getBlockZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.item.craft"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onCraftItem(CraftItemEvent event) {
					if (event.isCancelled())
						return;
					ItemStack crafted = event.getCurrentItem();
					int per = crafted.getAmount();
					int amt = per;
					if (event.isShiftClick()) {
						int min = new ItemStack(org.bukkit.Material.STONE).getMaxStackSize();
						for (int i = 1; i < event.getInventory().getSize(); ++i) {
							ItemStack temp = event.getInventory().getItem(i);
							if (temp == null)
								continue;
							int size = temp.getAmount();
							if (size < min)
								min = size;
						}
						amt = per * min;
						ItemStack[] inv = event.getWhoClicked().getInventory().getContents();
						int spaceFor = 0;
						for (ItemStack element : inv)
							if (element == null)
								spaceFor += crafted.getMaxStackSize();
							else if (element.getType() == crafted.getType() && element.getData().getData() == crafted.getData().getData())
								spaceFor += crafted.getMaxStackSize() - element.getAmount();
						if (spaceFor < amt)
							amt = spaceFor;
					}
					database.addMaterial(event.getWhoClicked().getName(), Material.Event.CRAFTED + "", Material.getMaterial(crafted) + "", amt + "", event.getWhoClicked().getWorld().getName(), event.getWhoClicked().getLocation().getBlockX() + "", event.getWhoClicked().getLocation().getBlockY() + "", event.getWhoClicked().getLocation().getBlockZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.item.drop"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onPlayerDropItem(PlayerDropItemEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.DROPPED + "", Material.getMaterial(event.getItemDrop()) + "", event.getItemDrop().getItemStack().getAmount() + "", event.getPlayer().getWorld().getName(), event.getPlayer().getLocation().getBlockX() + "", event.getPlayer().getLocation().getBlockY() + "", event.getPlayer().getLocation().getBlockZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.item.pickup"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onPlayerPickupItem(PlayerPickupItemEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.PICKED_UP + "", Material.getMaterial(event.getItem()) + "", event.getItem().getItemStack().getAmount() + "", event.getPlayer().getWorld().getName(), event.getPlayer().getLocation().getBlockX() + "", event.getPlayer().getLocation().getBlockY() + "", event.getPlayer().getLocation().getBlockZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.item.use"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.USED + "", Material.getMaterial(event.getItem()) + "", "1", event.getPlayer().getWorld().getName(), event.getPlayer().getLocation().getBlockX() + "", event.getPlayer().getLocation().getBlockY() + "", event.getPlayer().getLocation().getBlockZ() + "");
				}
			}, this);
		// Damage events
		if (configuration.getBoolean("events.damage"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onEntityDamage(EntityDamageEvent event) {
					if (event.isCancelled())
						return;
					// If the entity isn't a living entity we don't care
					if (!(event.getEntity() instanceof LivingEntity))
						return;
					// Get the values that always exist
					DamageCause type = event.getCause();
					double amount = event.getDamage();
					Location loc = event.getEntity().getLocation();
					LivingEntity damaged = (LivingEntity) event.getEntity();
					// Figure out who, if anybody, got hurt
					if (damaged.isDead())
						return;
					if (damaged.getNoDamageTicks() > damaged.getMaximumNoDamageTicks() / 2.0F)
						if (amount > damaged.getLastDamage())
							amount = amount - damaged.getLastDamage();
						else
							return;
					// Did they died
					boolean death = amount >= damaged.getHealth();
					// Who hurt them
					if (event instanceof EntityDamageByEntityEvent) {
						Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
						EntityDamageByEntityEvent eventByEntity = (EntityDamageByEntityEvent) event;
						if (eventByEntity.getCause() == DamageCause.ENTITY_ATTACK && damager.getType() == EntityType.PLAYER)
							database.addDamage(damager.getType() + "", ((Player) damager).getName(), Material.getMaterial(((Player) damager).getItemInHand()) + "", null, damaged.getType() + "", damaged.getType() == EntityType.PLAYER ? ((Player) damaged).getName() : null, death + "", type + "", amount + "", loc.getWorld().getName(), loc.getBlockX() + "", loc.getBlockY() + "", loc.getBlockZ() + "");
						if (!(eventByEntity.getDamager() instanceof LivingEntity)) {
							Projectile projectile = null;
							switch (eventByEntity.getDamager().getType())
							{
								case PRIMED_TNT:
									damager = ((TNTPrimed) eventByEntity.getDamager()).getSource();
									database.addDamage(damager.getType() + "", ((Player) damager).getName(), null, null, damaged.getType() + "", damaged.getType() == EntityType.PLAYER ? ((Player) damaged).getName() : null, death + "", type + "", amount + "", loc.getWorld().getName(), loc.getBlockX() + "", loc.getBlockY() + "", loc.getBlockZ() + "");
									break;
								case ENDER_PEARL:
								case SPLASH_POTION:
								case ARROW:
									projectile = (Projectile) damager;
									damager = (Entity) ((Projectile) eventByEntity.getDamager()).getShooter();
									database.addDamage(damager.getType() + "", ((Player) damager).getName(), null, projectile + "", damaged.getType() + "", damaged.getType() == EntityType.PLAYER ? ((Player) damaged).getName() : null, death + "", type + "", amount + "", loc.getWorld().getName(), loc.getBlockX() + "", loc.getBlockY() + "", loc.getBlockZ() + "");
									break;
								default:
									database.addDamage(damager.getType() + "", ((Player) damager).getName(), null, null, damaged + "", damaged.getType() == EntityType.PLAYER ? ((Player) damaged).getName() : null, death + "", type + "", amount + "", loc.getWorld().getName(), loc.getBlockX() + "", loc.getBlockY() + "", loc.getBlockZ() + "");
							}
						}
					} else
						database.addDamage(null, null, null, null, damaged.getType() + "", damaged.getType() == EntityType.PLAYER ? ((Player) damaged).getName() : null, death + "", type + "", amount + "", loc.getWorld().getName(), loc.getBlockX() + "", loc.getBlockY() + "", loc.getBlockZ() + "");
				}
			}, this);
		// Distance events
		if (configuration.getBoolean("events.distance"))
			pm.registerEvents(new Listener() {
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
					if (!jumping.get(name) && sy > 0 && !block.isLiquid() && !below.getType().isSolid() && !below.isLiquid() && below.getType() != org.bukkit.Material.LADDER && below.getType() != org.bukkit.Material.VINE)
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
					else if (block.getType() == org.bukkit.Material.LADDER || block.getType() == org.bukkit.Material.VINE)
						database.addDistance(name, Distance.CLIMBED + "", y + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
					else
						database.addDistance(name, Distance.WALKED + "", xyz + "", event.getFrom().getWorld().getName(), event.getFrom().getBlockX() + "", event.getFrom().getBlockY() + "", event.getFrom().getBlockZ() + "", event.getTo().getBlockX() + "", event.getTo().getBlockY() + "", event.getTo().getBlockZ() + "");
				}
			}, this);
	}

	private void extractResources() {
		boolean needsUpdate = false;
		File df = this.getDataFolder();
		if (!df.exists())
			df.mkdirs();
		File ver = new File(df, "version.txt");
		if (ver.exists()) {
			String current = null, previous = null;
			// Get current
			try {
				BufferedReader cr = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/extracted/version.txt")));
				current = cr.readLine();
				cr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Get previous
			try {
				BufferedReader pr = new BufferedReader(new FileReader(ver));
				previous = pr.readLine();
				if (!previous.equals(current))
					needsUpdate = true;
				pr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			needsUpdate = true;
		if (!needsUpdate)
			return;
		try {
			ZipFile zf = new ZipFile(new File(Webstats.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
			FileOutputStream fos = null;
			InputStream ins = null;
			byte[] buf = new byte[2048];
			File f;
			String n;
			Enumeration<? extends ZipEntry> e = zf.entries();
			while (e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				n = ze.getName();
				if (!n.startsWith("extracted/"))
					continue;
				n = n.substring("extracted/".length());
				f = new File(df, n);
				if (ze.isDirectory())
					f.mkdirs();
				else {
					f.getParentFile().mkdirs();
					fos = new FileOutputStream(f);
					ins = zf.getInputStream(ze);
					int len;
					while ((len = ins.read(buf)) >= 0)
						fos.write(buf, 0, len);
					ins.close();
					fos.close();
				}
			}
			zf.close();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
