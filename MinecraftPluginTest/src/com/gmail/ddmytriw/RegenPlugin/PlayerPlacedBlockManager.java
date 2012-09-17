package com.gmail.ddmytriw.RegenPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class PlayerPlacedBlockManager implements Listener {
	static String INTERNAL_WORLD_NAME_SUFFIX = "_player_placed";
	String fullWorldName;
	World ppWorld;

	public PlayerPlacedBlockManager(World world) {
		super();
		fullWorldName = world.getName() + INTERNAL_WORLD_NAME_SUFFIX;
	}

	public void onEnable() {
		RegenPlugin.get().getServer().getPluginManager().registerEvents((Listener) this, RegenPlugin.get());	

		ppWorld = RegenPlugin.get().getServer().getWorld(fullWorldName);
		if(null == ppWorld)
		{
			RegenPlugin.get().getLogger().info("creating new World: " + fullWorldName);
			WorldCreator wc = new WorldCreator(fullWorldName);
			wc.generator(new EmptyChunkGenerator());
			ppWorld = wc.createWorld();
		}
		
		ppWorld.setSpawnFlags(false, false);
		ppWorld.setAutoSave(false);
		ppWorld.setKeepSpawnInMemory(false);
		
		RegenPlugin.get().getLogger().info("PlayerPlacedWorld ready! - " + fullWorldName);
	}

	public void onDisable() {
		HandlerList.unregisterAll((Listener)this);
	}
		
	
	public Block getBlockAt(Location location) {
		return ppWorld.getBlockAt(location);
	}

	public void onBlockPlacedByPlayer(Block block) {
		Block pp_block = getBlockAt(block.getLocation());
		pp_block.setTypeId(block.getTypeId());
	}

	public boolean isBlockPlacedByPlayer(Block block) {
		Block pp_block = getBlockAt(block.getLocation());
		return pp_block.getType() != Material.AIR;
	}

	public void onBlockRemoved(Block block) {
		Block pp_block = getBlockAt(block.getLocation());
		pp_block.setType(Material.AIR);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockChange(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld().getName().compareTo(fullWorldName) == 0){
        	event.setCancelled(true);
        	RegenPlugin.get().getLogger().info("BlockPlaceEvent canceled! - " + event.toString());
        }
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockChange(BlockBreakEvent event){
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld().getName().compareTo(fullWorldName) == 0){
        	event.setCancelled(true);
        	RegenPlugin.get().getLogger().info("BlockPlaceEvent canceled! - " + event.toString());        	
        }        
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockChange(EntityExplodeEvent event){
        if (event.isCancelled()) return;
        if (event.getEntity().getWorld().getName().compareTo(fullWorldName) == 0){
        	event.setCancelled(true);
        	RegenPlugin.get().getLogger().info("BlockPlaceEvent canceled! - " + event.toString());
        }        
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCreatureSpawn(CreatureSpawnEvent event){
        if (event.isCancelled()) return;
        if (event.getEntity().getWorld().getName().compareTo(fullWorldName) == 0){
        	event.setCancelled(true);
        	//RegenPlugin.get().getLogger().info("CreatureSpawnEvent canceled! - " + event.toString());
        }        
	}
}
