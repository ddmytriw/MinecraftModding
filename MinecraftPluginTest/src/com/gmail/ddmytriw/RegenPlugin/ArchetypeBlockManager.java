package com.gmail.ddmytriw.RegenPlugin;

import org.bukkit.Location;
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
import org.bukkit.event.world.WorldInitEvent;

public class ArchetypeBlockManager implements Listener{
	static String INTERNAL_WORLD_NAME_SUFFIX = "_archetype";
	World originalWorld;
	World archetypeWorld;
	String archetypeWorldName;

	public ArchetypeBlockManager(World world) {
		super();
		assert(world != null);
		originalWorld = world;
		archetypeWorldName = originalWorld.getName() + INTERNAL_WORLD_NAME_SUFFIX;
	}

	public void onEnable() {
		RegenPlugin.get().getServer().getPluginManager().registerEvents((Listener) this, RegenPlugin.get());

		archetypeWorld = RegenPlugin.get().getServer().getWorld(archetypeWorldName);
		if(null == archetypeWorld)
		{
			RegenPlugin.get().getLogger().info("creating new World: " + archetypeWorldName);
			WorldCreator wc = new WorldCreator(archetypeWorldName);
			wc.copy(originalWorld);
			//wc.generateStructures(false); //TODO: test generateStructures flag in archetype world. structures shouldn't regenerate should they?
			archetypeWorld = wc.createWorld();
		}
			
		RegenPlugin.get().getLogger().info("ArchetypeWorld ready! - " + archetypeWorldName);
	}

	public void onDisable() {
		HandlerList.unregisterAll((Listener)this);
	}
		
	public Block getBlockAt(Location location) {
		assert(archetypeWorld != null);
		return archetypeWorld.getBlockAt(location);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockChange(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld().getName().compareTo(archetypeWorldName) == 0){
        	event.setCancelled(true);
        	RegenPlugin.get().getLogger().info("BlockPlaceEvent canceled! - " + event.toString());
        }
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockChange(BlockBreakEvent event){
        if (event.isCancelled()) return;
        if (event.getBlock().getWorld().getName().compareTo(archetypeWorldName) == 0){
        	event.setCancelled(true);
        	RegenPlugin.get().getLogger().info("BlockPlaceEvent canceled! - " + event.toString());        	
        }        
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockChange(EntityExplodeEvent event){
        if (event.isCancelled()) return;
        if (event.getEntity().getWorld().getName().compareTo(archetypeWorldName) == 0){
        	event.setCancelled(true);
        	RegenPlugin.get().getLogger().info("BlockPlaceEvent canceled! - " + event.toString());
        }        
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onCreatureSpawn(CreatureSpawnEvent event){
        if (event.isCancelled()) return;
        if (event.getEntity().getWorld().getName().compareTo(archetypeWorldName) == 0){
        	event.setCancelled(true);
        	//RegenPlugin.get().getLogger().info("CreatureSpawnEvent canceled! - " + event.toString());
        }        
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onWorldInit(WorldInitEvent event){
		World world = event.getWorld();

		world.setSpawnFlags(false, false);
		world.setAutoSave(false);
		//world.setKeepSpawnInMemory(false);//TODO: investigate World flags
		
        if (world.getName().compareTo(archetypeWorldName) == 0){
        	RegenPlugin.get().getLogger().info("WorldInitEvent - " + world.getName());
        	world.getPopulators().add(new RegenableBlockPopulator());
        }        
	}
}
