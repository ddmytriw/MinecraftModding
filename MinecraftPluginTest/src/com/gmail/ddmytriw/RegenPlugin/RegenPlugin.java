package com.gmail.ddmytriw.RegenPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

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

public class RegenPlugin extends JavaPlugin implements Listener {

	private int regenTaskId = -1;
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
		getCommand("regen").setExecutor(new PluginCommandExecutor(this));
		
		this.getServer().getPluginManager().registerEvents(this, this);
		
		StartRegen();
	}
	
	public void RegenTask()
	{
		//getLogger().info(this.getName() + ".RegenTask()");

		if(!modified_block_list.isEmpty())
		{
			//sort by Y, this will move lowest blocks in terrain to front of regen 'queue'
			Collections.sort(modified_block_list, new blockSortingComparator());
			
			Block block = modified_block_list.remove(0);
			regenBlock(block);
		}
	}

	public void StartRegen()
	{
		if(regenTaskId == -1)
		{
			getLogger().info(this.getName() + " Regen task activated!");
			
			Runnable regen_task = new Runnable(){
				@Override
				public void run() {
					RegenTask();
				}
			};
			
			regenTaskId = this.getServer().getScheduler().scheduleSyncRepeatingTask(this, regen_task, 0L, 20L);
		}
		else
		{
			getLogger().info(this.getName() + " Error: Regen task already active. Use '/regen stop' to stop task.");
		}
	}
	
	public void StopRegen()
	{
		if(regenTaskId != -1)
		{
			getLogger().info(this.getName() + " Regen task stopped!");
			this.getServer().getScheduler().cancelTask(regenTaskId);
		}
		else
		{
			getLogger().info(this.getName() + " Error: Regen task wasn't running!");
		}
	}
	
	public void SaveBlockList()
	{
		getLogger().info(this.getName() + ".SaveBlockList()");
		
	}
	
	public void LoadBlockList()
	{
		getLogger().info(this.getName() + ".LoadBlockList()");

	}

	public class blockSortingComparator implements Comparator<Block>{
		@Override
	    public int compare(Block block1, Block block2) {
	        return block1.getY() - block2.getY();
	    }
	}

	static String KEY_ORIGINAL_TYPE_ID = "original_type_id";
	static String KEY_PLACED_BY_PLAYER = "placed_by_player";	
	private void setBlockPlacedByPlayer(Block block)
	{
		//check that block is original(from the original world generation and not placed by a player or entity)
		if(!block.hasMetadata(KEY_PLACED_BY_PLAYER)){
			block.setMetadata(KEY_PLACED_BY_PLAYER, new FixedMetadataValue(this, true));
		}
	}
	
	private boolean isBlockPlacedByPlayer(Block block)
	{
		return block.hasMetadata(KEY_PLACED_BY_PLAYER);
	}
	
	private void clearBlockPlacedByPlayerMetadata(Block block)
	{
		if(!block.hasMetadata(KEY_PLACED_BY_PLAYER)){
			block.removeMetadata(KEY_PLACED_BY_PLAYER, this);
		}
	}

	public void onBlockRemoved(Block block){
		//check that block is original(from the original world generation and not placed by a player or entity)
		if(isBlockPlacedByPlayer(block)){
			clearBlockPlacedByPlayerMetadata(block);
		}
		else
		{
			if(!block.hasMetadata(KEY_ORIGINAL_TYPE_ID)){
				block.setMetadata(KEY_ORIGINAL_TYPE_ID, new FixedMetadataValue(this, block.getTypeId()));
			}
			//TODO: check that block is 'regeneratable' (ie, not plant life)		
			modified_block_list.add(block);	
		}
	}
	
	private void regenBlock(Block block)
	{
		assert(block.hasMetadata(KEY_ORIGINAL_TYPE_ID));
		
		List<MetadataValue> values = block.getMetadata(KEY_ORIGINAL_TYPE_ID);
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){ //do we need to do this check? seems inefficient
				int block_type = value.asInt();
				//getLogger().info("changing block at " + block.getLocation().toString() + " from:" + block.getTypeId() + " to:" + block_type);
				block.setTypeId(block_type);				
				block.removeMetadata(KEY_ORIGINAL_TYPE_ID, this);
				return;
			}
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
		
		setBlockPlacedByPlayer(block);
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
		
		this.onBlockRemoved(block);
		
		block.breakNaturally(event.getPlayer().getItemInHand());
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
