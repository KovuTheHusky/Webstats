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
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
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
		pm.registerEvents(new PlayerListener(database), this);
		if (configuration.getBoolean("events.block.break"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockBreak(BlockBreakEvent event) {
					Material m = Material.getMaterial(event.getBlock());
					Bukkit.broadcastMessage("Break: " + m);
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.BLOCK_BREAK + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.burn"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockBurn(BlockBurnEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BLOCK_BURN + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.damage"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockDamage(BlockDamageEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.BLOCK_DAMAGE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.dispense"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockDispense(BlockDispenseEvent event) {
					if (event.isCancelled())
						return;
					database.addMaterial(null, Material.Event.BLOCK_DISPENSE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
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
					database.addMaterial(null, Material.Event.BLOCK_FADE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
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
					database.addMaterial(null, Material.Event.BLOCK_GROW + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
				}
			}, this);
		if (configuration.getBoolean("events.block.ignite"))
			pm.registerEvents(new Listener() {
				@EventHandler
				public void onBlockIgnite(BlockIgniteEvent event) {
					if (event.isCancelled())
						return;
					if (event.getPlayer() != null)
						database.addMaterial(event.getPlayer().getName(), Material.Event.BLOCK_IGNITE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
					else
						database.addMaterial(null, Material.Event.BLOCK_IGNITE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
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
					Material m = Material.getMaterial(event.getBlock());
					Bukkit.broadcastMessage("Place: " + m);
					if (event.isCancelled())
						return;
					database.addMaterial(event.getPlayer().getName(), Material.Event.BLOCK_PLACE + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
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
		if (configuration.getBoolean("events.block.craft"))
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
							else if (element.getType() == crafted.getType()) // TODO: Make sure this works for subtypes of types like different wood types.
								spaceFor += crafted.getMaxStackSize() - element.getAmount();
						if (spaceFor < amt)
							amt = spaceFor;
					}
					database.addMaterial(event.getWhoClicked().getName(), Material.Event.ITEM_CRAFT + "", Material.getMaterial(crafted) + "", amt + "", event.getWhoClicked().getWorld().getName(), event.getWhoClicked().getLocation().getBlockX() + "", event.getWhoClicked().getLocation().getBlockY() + "", event.getWhoClicked().getLocation().getBlockZ() + "");
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
					database.addMaterial(null, Material.Event.LEAVES_DECAY + "", Material.getMaterial(event.getBlock()) + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
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
