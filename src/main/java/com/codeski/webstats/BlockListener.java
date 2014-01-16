package com.codeski.webstats;

import org.bukkit.Material;
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

import com.codeski.webstats.databases.Database;

public class BlockListener implements Listener {
	private final Database database;

	public BlockListener(Database database) {
		this.database = database;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(event.getPlayer().getName(), MaterialEvent.BLOCK_BREAK + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockBurn(BlockBurnEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_BURN + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(event.getPlayer().getName(), MaterialEvent.BLOCK_DAMAGE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_DISPENSE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockExp(BlockExpEvent event) {
		database.insertMaterial(null, MaterialEvent.BLOCK_EXP + "", event.getBlock().getType() + "", event.getExpToDrop() + "", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockFade(BlockFadeEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_FADE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_FROM_TO + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockGrow(BlockGrowEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_GROW + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled())
			return;
		if (event.getPlayer() != null)
			database.insertMaterial(event.getPlayer().getName(), MaterialEvent.BLOCK_IGNITE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
		else
			database.insertMaterial(null, MaterialEvent.BLOCK_IGNITE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_PHYSICS + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockPistonExtend(BlockPistonExtendEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_PISTON_EXTEND + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockPistonRetract(BlockPistonRetractEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BLOCK_PISTON_RETRACT + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(event.getPlayer().getName(), MaterialEvent.BLOCK_PLACE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event) {
		database.insertMaterial(null, MaterialEvent.BLOCK_REDSTONE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onBrew(BrewEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.BREW + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		if (event.isCancelled())
			return;
		ItemStack crafted = event.getCurrentItem();
		int per = crafted.getAmount();
		int amt = per;
		if (event.isShiftClick()) {
			int min = new ItemStack(Material.STONE).getMaxStackSize(); // TODO: Should this be stone or what you make?
			amt = per * min;
			for (int i = 1; i < event.getInventory().getSize(); ++i) {
				ItemStack temp = event.getInventory().getItem(i);
				if (temp == null)
					continue;
				int size = temp.getAmount();
				Webstats.info(i + ": " + size + " " + temp.getType());
				if (size < min)
					min = size;
			}
			ItemStack[] inv = event.getWhoClicked().getInventory().getContents();
			int spaceFor = 0;
			for (ItemStack element : inv)
				if (element == null)
					spaceFor += crafted.getMaxStackSize();
				else if (element.getType() == crafted.getType())
					spaceFor += crafted.getMaxStackSize() - element.getAmount();
			if (spaceFor < amt)
				amt = spaceFor; // - spaceFor % per;
		}
		database.insertMaterial(event.getWhoClicked().getName(), MaterialEvent.ITEM_CRAFT + "", crafted.getType() + "", amt + "", event.getWhoClicked().getWorld().getName(), event.getWhoClicked().getLocation().getBlockX() + "", event.getWhoClicked().getLocation().getBlockY() + "", event.getWhoClicked().getLocation().getBlockZ() + "");
	}

	@EventHandler
	public void onFurnaceBurn(FurnaceBurnEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.FURNACE_BURN + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.FURNACE_SMELT + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.LEAVES_DECAY + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onNotePlay(NotePlayEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(null, MaterialEvent.NOTE_PLAY + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled())
			return;
		database.insertMaterial(event.getPlayer().getName(), MaterialEvent.SIGN_CHANGE + "", event.getBlock().getType() + "", "1", event.getBlock().getWorld().getName(), event.getBlock().getX() + "", event.getBlock().getY() + "", event.getBlock().getZ() + "");
	}
}
