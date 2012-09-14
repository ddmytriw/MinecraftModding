package com.gmail.ddmytriw.RegenPlugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;

public class PlayerPlacedWorld {
	static String WORLD_NAME = "pp_world";
	World world;

	public PlayerPlacedWorld(String actual_world_name) {
		super();

		World actual_world = RegenPlugin.get().getServer().getWorld(actual_world_name);
		assert(actual_world != null);		
		world = RegenPlugin.get().getServer().getWorld(WORLD_NAME);
		if(null == world)
		{
			RegenPlugin.get().getLogger().info("creating new World: " + WORLD_NAME);
			WorldCreator wc = new WorldCreator(WORLD_NAME);
			wc.generator(new EmptyChunkGenerator());
			world = wc.createWorld();
		}
		
		world.setSpawnFlags(false, false);
		world.setAutoSave(false);
		world.setKeepSpawnInMemory(false);
		
		RegenPlugin.get().getLogger().info("PlayerPlacedWorld ready!");
	}

	public Block getBlockAt(Location location) {
		return world.getBlockAt(location);
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
	
	
	
}
