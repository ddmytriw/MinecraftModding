package com.gmail.ddmytriw.RegenPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class Regenerator implements Runnable {
	static String KEY_ORIGINAL_TYPE_ID = "original_type_id";
	static String KEY_PLACED_BY_PLAYER = "placed_by_player";	
	private Plugin plugin;
	private List<Block> regenBlockList = new ArrayList<Block>();
	private List<Block> playerPlacedBlockList = new ArrayList<Block>();
	
	public Regenerator(Plugin plugin) {
		super();
		this.plugin = plugin;
		loadBlockList();
	}

	@Override
	public void run() {
		regenTask();
	}

	public void onEnable()
	{
		plugin.getLogger().info("Regenerator.onEnable()");
		loadBlockList();
	}
	
	public void onDisable() 
	{
		plugin.getLogger().info("Regenerator.onDisable()");
		saveBlockList();
	}

	public void regenTask()
	{
		//getLogger().info(this.getName() + ".RegenTask()");

		if(!regenBlockList.isEmpty())
		{
			//sort by Y, this will move lowest blocks in terrain to front of regen 'queue'
			Collections.sort(regenBlockList, new lowestFirstComparator());

			ListIterator<Block> iter = regenBlockList.listIterator();
			while (iter.hasNext()) {
				Block block = (Block) iter.next();
				if(!isBlockPlacedByPlayer(block)){
					regenBlockList.remove(block);
					regenBlock(block);
					break;					
				}
			}	
		}
	}
	
	public void saveBlockList()
	{
		plugin.getLogger().info(plugin.getName() + ".SaveBlockList()");
		
	}
	
	public void loadBlockList()
	{
		plugin.getLogger().info(plugin.getName() + ".LoadBlockList()");

	}

	//lowest block first
	public class lowestFirstComparator implements Comparator<Block>{
		@Override
	    public int compare(Block block1, Block block2) {
	        return block1.getY() - block2.getY();
	    }
	}

	public void onBlockRemoved(Block block){
		plugin.getLogger().info("onBlockRemoved: " + block.getLocation().toString());
		//check that block is original(from the original world generation and not placed by a player or entity)
		if(isBlockPlacedByPlayer(block)){
			clearBlockPlacedByPlayerMetadata(block);
		}
		else
		{
			//TODO: check that block is 'regeneratable' (ie. not plant life)		
			addBlockToRegenList(block);
		}
	}
	
	public void onBlockRemoved(List<Block> block_list){
		ListIterator<Block> iter = block_list.listIterator();
		while (iter.hasNext()) {
			Block block = (Block) iter.next();
			onBlockRemoved(block);
		}		
	}

	private void addBlockToRegenList(Block block) {
		if(!block.hasMetadata(KEY_ORIGINAL_TYPE_ID)){
			block.setMetadata(KEY_ORIGINAL_TYPE_ID, new FixedMetadataValue(plugin, block.getTypeId()));
		}
		plugin.getLogger().info("addBlockToRegenList - " + block.getLocation().toString());
		regenBlockList.add(block);	
	}
	
	private void regenBlock(Block block)
	{
		plugin.getLogger().info("regenBlock: " + block.getLocation().toString());
		
		assert(block.hasMetadata(KEY_ORIGINAL_TYPE_ID));
		
		List<MetadataValue> values = block.getMetadata(KEY_ORIGINAL_TYPE_ID);
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())){ //do we need to do this check? seems inefficient
				int block_type = value.asInt();
				plugin.getLogger().info("     regenerating block at " + block.getLocation().toString() + " from:" + block.getTypeId() + " to:" + block_type);
				block.setTypeId(block_type);				
				block.removeMetadata(KEY_ORIGINAL_TYPE_ID, plugin);
				return;
			}
		}		
	}	
	
	public void onBlockPlacedByPlayer(Block block)
	{
		plugin.getLogger().info("onBlockPlacedByPlayer - " + block.getLocation().toString());

		//add to list so we can save it
		if(!playerPlacedBlockList.contains(block))
		{
			playerPlacedBlockList.add(block);
		}
	}
	
	private boolean isBlockPlacedByPlayer(Block block)
	{
		return playerPlacedBlockList.contains(block);
	}
	
	private void clearBlockPlacedByPlayerMetadata(Block block)
	{
		plugin.getLogger().info("clearBlockPlacedByPlayerMetadata - " + block.getLocation().toString());
		
		playerPlacedBlockList.remove(block);
	}
}
