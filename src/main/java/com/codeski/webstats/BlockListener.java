package com.codeski.webstats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

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
}
