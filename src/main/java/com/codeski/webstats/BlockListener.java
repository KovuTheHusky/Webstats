package com.codeski.webstats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
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
		database.blockBroken(event.getPlayer(), event.getBlock().getTypeId(), event.getBlock().getData());
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		database.blockPlaced(event.getPlayer(), event.getBlock().getTypeId(), event.getBlock().getData());
	}

	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		if (event.isCancelled())
			return;
		ItemStack crafted = event.getCurrentItem();
		int per = crafted.getAmount();
		int amt = per;
		if (event.isShiftClick()) {
			int min = new ItemStack(1).getMaxStackSize();
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
		if (crafted.getType().isBlock())
			database.blockCrafted(event.getWhoClicked(), crafted.getTypeId(), crafted.getData().getData(), amt);
	}
}
