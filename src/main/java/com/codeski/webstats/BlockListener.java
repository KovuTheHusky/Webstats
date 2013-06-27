package com.codeski.webstats;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		UUID uuid = event.getPlayer().getUniqueId();
		int b = event.getBlock().getTypeId();
		byte d = event.getBlock().getData();
		int count = Database.update("UPDATE ws_blocks SET block_broken = block_broken + 1 WHERE block_id = " + b + " AND block_data = " + d + " AND block_player = '" + uuid + "'");
		if (count < 1)
			Database.update("INSERT INTO ws_blocks VALUES (" + b + ", " + d + ", 1, 0, 0, '" + uuid + "')");
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		UUID uuid = event.getPlayer().getUniqueId();
		int b = event.getBlock().getTypeId();
		byte d = event.getBlock().getData();
		int count = Database.update("UPDATE ws_blocks SET block_placed = block_placed + 1 WHERE block_id = " + b + " AND block_data = " + d + " AND block_player = '" + uuid + "'");
		if (count < 1)
			Database.update("INSERT INTO ws_blocks VALUES (" + b + ", " + d + ", 0, 0, 1, '" + uuid + "')");
	}
}
