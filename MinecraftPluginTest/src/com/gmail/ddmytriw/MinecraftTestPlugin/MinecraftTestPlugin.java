package com.gmail.ddmytriw.MinecraftTestPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
					SortBlockList();
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
	
	public class blockSortingComparator implements Comparator<Block>{
		@Override
	    public int compare(Block block1, Block block2) {
	        return block1.getY() - block2.getY();
	    }
	}
	
	public void SortBlockList()
	{		
		//sort by Y, this will move lowest blocks in terrain to front of regen 'queue'
		Collections.sort(modified_block_list, new blockSortingComparator());
	}

	static String BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID = "original_type_id";
	static String BLOCK_METADATA_KEY_PLACED_BY_PLAYER = "placed_by_player";
	private void SetBlockRegenMetadata(Block block)
	{
		//check that block is original(from the original world generation and not placed by a player or entity)
		if(!block.hasMetadata(BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID)){
			block.setMetadata(BLOCK_METADATA_KEY_ORIGINAL_TYPE_ID, new FixedMetadataValue(this, block.getTypeId()));
		}
	}

	private void SetBlockPlayerPlacedMetadata(Block block)
	{
		//check that block is original(from the original world generation and not placed by a player or entity)
		if(!block.hasMetadata(BLOCK_METADATA_KEY_PLACED_BY_PLAYER)){
			block.setMetadata(BLOCK_METADATA_KEY_PLACED_BY_PLAYER, new FixedMetadataValue(this, true));
		}
	}
	
	private boolean IsBlockPlayerPlaced(Block block)
	{
		return block.hasMetadata(BLOCK_METADATA_KEY_PLACED_BY_PLAYER);
	}	
	
	private void ResetBlockPlayerPlacedMetadata(Block block)
	{
		if(!block.hasMetadata(BLOCK_METADATA_KEY_PLACED_BY_PLAYER)){
			block.removeMetadata(BLOCK_METADATA_KEY_PLACED_BY_PLAYER, this);
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
	
	public void onBlockRemoved(Block block){
		if(IsBlockPlayerPlaced(block)){
			ResetBlockPlayerPlacedMetadata(block);
		}
		else
		{
			SetBlockRegenMetadata(block);
			//TODO: check that block is 'regeneratable' (ie, not plant life)		
			modified_block_list.add(block);	
		}
	}
	
	public void onBlockRemoved(List<Block> block_list){
		ListIterator<Block> iter = block_list.listIterator();
		while (iter.hasNext()) {
			Block block = (Block) iter.next();
			onBlockRemoved(block);
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        
		Player player = event.getPlayer();
		Block block = event.getBlockPlaced();
		getLogger().info(event.getEventName() + ": " + player.getDisplayName() + " placed a block id: " + block.getTypeId() + " at x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
		
		SetBlockPlayerPlacedMetadata(block);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(BlockBreakEvent event){
        if (event.isCancelled()) return;
        
		Player player = event.getPlayer();
		Block block = event.getBlock();
		getLogger().info(event.getEventName() + ": " + player.getDisplayName() + " broke a block id: " + block.getTypeId() + " at x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
		
		this.onBlockRemoved(event.getBlock());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockChange(EntityExplodeEvent event){
        if (event.isCancelled()) return;
        
		getLogger().info(event.getEventName() + ": " + event.getEntityType().getName());

		List<Block> block_list = event.blockList();
		this.onBlockRemoved(block_list);
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
