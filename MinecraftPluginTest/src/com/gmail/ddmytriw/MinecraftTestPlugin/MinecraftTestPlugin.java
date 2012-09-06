package com.gmail.ddmytriw.MinecraftTestPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftTestPlugin extends JavaPlugin implements Listener {
	
	private List<Block> destroyed_block_list = new ArrayList<Block>();

	@Override
	public void onDisable() {
		super.onDisable();
		getLogger().info(this.getName() + "onDisable has been invoked!");
		
		HandlerList.unregisterAll((Listener)this);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		getLogger().info(this.getName() + "onEnable has been invoked!");
		
		// This will throw a NullPointException if you don't have the command defined in your plugin.yml file!
		getCommand("testplugin").setExecutor(new TestPluginCommandExecutor(this));
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		// debug stuff
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			@Override
			public void run() {
				getLogger().info("scheduleSyncRepeatingTask.run()");
				
				ListIterator<Block> iter = destroyed_block_list.listIterator();
				while (iter.hasNext()) {
					Block block = (Block) iter.next();
					getLogger().info(iter.hashCode() + ": x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
				}
				
			}
		}, 0L, 20L*10L);
		
		/*this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
			@Override
			public void run() {
				getLogger().info("scheduleAsyncRepeatingTask.run()");
			}
		}, 0L, 20L*10L);*/
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockChange(BlockBreakEvent event){
		Player player = event.getPlayer();
		Block block = event.getBlock();
		getLogger().info(event.getEventName() + ": " + player.getDisplayName() + " broke a block id: " + block.getTypeId() + " at x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
		
		destroyed_block_list.add(event.getBlock());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockChange(BlockDamageEvent event){
		getLogger().info(event.getEventName());
		
		destroyed_block_list.add(event.getBlock());
	}
		
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockChange(BlockFadeEvent event){
		//getLogger().info(event.getEventName());

		//destroyed_block_list.add(event.getBlock());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onBlockChange(EntityExplodeEvent event){
		getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());

		List<Block> block_list = event.blockList();
		destroyed_block_list.addAll(block_list);
	}
	
}
