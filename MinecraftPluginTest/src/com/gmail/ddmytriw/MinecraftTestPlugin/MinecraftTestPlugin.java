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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftTestPlugin extends JavaPlugin implements Listener {
	
	private List<Block> modified_block_list = new ArrayList<Block>();

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
				//getLogger().info("scheduleSyncRepeatingTask.run()");
				
				if(!modified_block_list.isEmpty())
				{
					Block block = modified_block_list.remove(0);
					RegenBlock(block);
				}
				
			}
		}, 0L, 20L);
		
		/*this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable(){
			@Override
			public void run() {
				getLogger().info("scheduleAsyncRepeatingTask.run()");
			}
		}, 0L, 20L*10L);*/
	}
	
	public void PrintBlockList()
	{
		getLogger().info(this.getName() + ".PrintBlockList()");
		
		ListIterator<Block> iter = modified_block_list.listIterator();
		while (iter.hasNext()) {
			Block block = (Block) iter.next();
			getLogger().info(iter.hashCode() + ": x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
		}
	}

	static String BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID = "original_type_id";
	private void SetBlockMetadata(Block block)
	{
		//check that block is original(from the original world generation and not placed by a player or entity)
		if(!block.hasMetadata(BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID)){
			block.setMetadata(BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID, new FixedMetadataValue(this, block.getTypeId()));
		}
	}
	
	private void RegenBlock(Block block)
	{
		assert(block.hasMetadata(BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID));
		
		List<MetadataValue> values = block.getMetadata(BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID);
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){ //do we need to do this check? seems inefficient
				int block_type = value.asInt();
				getLogger().info("changing block at " + block.getLocation().toString() + " from:" + block.getTypeId() + " to:" + block_type);
				block.setTypeId(block_type);				
				block.removeMetadata(BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID, this);
				return;
			}
		}
		
	}
	
	public void onBlockChange(Block block){
		SetBlockMetadata(block);
		//TODO: check that block is 'regeneratable' (ie, not plant life)		
		modified_block_list.add(block);
	}
	
	public void onBlockChange(List<Block> block_list){
		ListIterator<Block> iter = block_list.listIterator();
		while (iter.hasNext()) {
			Block block = (Block) iter.next();
			onBlockChange(block);
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(BlockBreakEvent event){
        if (event.isCancelled()) return;
        
		Player player = event.getPlayer();
		Block block = event.getBlock();
		getLogger().info(event.getEventName() + ": " + player.getDisplayName() + " broke a block id: " + block.getTypeId() + " at x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
		
		this.onBlockChange(event.getBlock());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(EntityExplodeEvent event){
        if (event.isCancelled()) return;
        
		getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());

		List<Block> block_list = event.blockList();
		this.onBlockChange(block_list);
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
//		modified_block_list.add(event.getBlock());
//	}
//		
//	@EventHandler(priority = EventPriority.LOW)
//	public void onBlockChange(EntityChangeBlockEvent event){
//        if (event.isCancelled()) return;
//        
//		getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());
//
//		this.onBlockChange(event.getBlock());
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
