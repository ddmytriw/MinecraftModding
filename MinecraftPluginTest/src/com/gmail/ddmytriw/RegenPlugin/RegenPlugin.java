package com.gmail.ddmytriw.RegenPlugin;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RegenPlugin extends JavaPlugin implements Listener {
	public long DEFAULT_TASK_PERIOD = 20L;//in ticks

	private int regenTaskId = -1;
	
	private Regenerator regenerator;

	@Override
	public void onEnable() {
		super.onEnable();
		getLogger().info(this.getName() + "onEnable has been invoked!");
		
		// This will throw a NullPointException if you don't have the command defined in your plugin.yml file!
		getCommand("regen").setExecutor(new PluginCommandExecutor(this));
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		regenerator = new Regenerator(this);
		regenerator.onEnable();
		
		StartRegen(DEFAULT_TASK_PERIOD);
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		getLogger().info(this.getName() + "onDisable has been invoked!");
		
		HandlerList.unregisterAll((Listener)this);
		
		regenerator.onDisable();
		regenerator = null;
	}
	
	public void StartRegen(long period_in_ticks)
	{
		if(regenTaskId == -1)
		{
			getLogger().info(this.getName() + " Regen task activated!");
			
			regenTaskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, regenerator, 0L, period_in_ticks);
		}
		else
		{
			StopRegen();
			StartRegen(period_in_ticks);
		}
	}
	
	public void StopRegen()
	{
		if(regenTaskId != -1)
		{
			getLogger().info(this.getName() + " Regen task stopped!");
			this.getServer().getScheduler().cancelTask(regenTaskId);
			regenTaskId = -1;
		}
		else
		{
			getLogger().info(this.getName() + " Error: Regen task wasn't running!");
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        
		Player player = event.getPlayer();
		Block block = event.getBlockPlaced();
		getLogger().info(event.getEventName() + ": " + player.getDisplayName() + " placed a block id: " + block.getTypeId() + " at x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
		
		regenerator.onBlockPlacedByPlayer(block);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(BlockBreakEvent event){
        if (event.isCancelled()) return;
        
		Block block = event.getBlock();
		getLogger().info(event.getEventName() + ": "
		+ event.getPlayer().getDisplayName()
		+ " broke a block id: "
		+ block.getTypeId()
		+ " at x:" + block.getX()
		+ " y:" + block.getY()
		+ " z:" + block.getZ()
		+ " with:" + event.getPlayer().getItemInHand().toString());
		
		regenerator.onBlockRemoved(block);
		
		block.breakNaturally(event.getPlayer().getItemInHand());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(EntityExplodeEvent event){
        if (event.isCancelled()) return;

		List<Block> block_list = event.blockList();
		if(block_list.size() > 0)
		{
			getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());
			regenerator.onBlockRemoved(block_list);
		}
	}
	
//	@EventHandler(priority = EventPriority.LOW)
//	public void onBlockChange(BlockDamageEvent event){
//        if (event.isCancelled()) return;
//        
//		getLogger().info(event.getEventName());
//	}
//		
//	@EventHandler(priority = EventPriority.LOW)
//	public void onBlockChange(BlockFadeEvent event){
//        if (event.isCancelled()) return;
//        
//		getLogger().info(event.getEventName());
//		event.getNewState().
//	}
//		
//	@EventHandler(priority = EventPriority.LOW)
//	public void onBlockChange(EntityChangeBlockEvent event){
//        if (event.isCancelled()) return;
//        
//		getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());
//	}
//	
//	@EventHandler(priority = EventPriority.LOW)
//	public void onChunkLoaded(ChunkLoadEvent event){
//		if(event.isNewChunk()){
//			getLogger().info(event.getEventName() + " loc:" + event.getChunk().getX() + "," + event.getChunk().getZ() + " - new chunk!");
//		}
//	}
//	
//	@EventHandler(priority = EventPriority.LOW)
//	public void onChunkLoaded(ChunkPopulateEvent event){
//		getLogger().info(event.getEventName() + ": loc:" + event.getChunk().getX() + "," + event.getChunk().getZ());
//	}
}
