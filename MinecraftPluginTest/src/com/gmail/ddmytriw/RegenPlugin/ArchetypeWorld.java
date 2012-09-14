package com.gmail.ddmytriw.RegenPlugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;

public class ArchetypeWorld {
	static String WORLD_NAME = "archetype_world";
	World world;

	public ArchetypeWorld(String actual_world_name) {
		super();

		World actual_world = RegenPlugin.get().getServer().getWorld(actual_world_name);
		assert(actual_world != null);		
		world = RegenPlugin.get().getServer().getWorld(WORLD_NAME);
		if(null == world)
		{
			RegenPlugin.get().getLogger().info("creating new World: " + WORLD_NAME);
			WorldCreator wc = new WorldCreator(WORLD_NAME);
			wc.copy(actual_world);
			world = wc.createWorld();
		}
		
		world.setSpawnFlags(false, false);
		world.setAutoSave(false);
		world.setKeepSpawnInMemory(false);
		
		RegenPlugin.get().getLogger().info("ArchetypeWorld ready!");
	}

	public Block getBlockAt(Location location) {
		return world.getBlockAt(location);
	}
	
}
