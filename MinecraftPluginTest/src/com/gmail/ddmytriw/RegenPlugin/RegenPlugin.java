package com.gmail.ddmytriw.RegenPlugin;

import java.util.List;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.plugin.java.JavaPlugin;

public class RegenPlugin extends JavaPlugin{
	static public RegenPlugin plugin;
	
	//Singleton
	static public RegenPlugin get(){
		assert (plugin != null);
		return plugin;
	}
	
	public long DEFAULT_TASK_PERIOD = 20L;//in ticks
	private int regenTaskId = -1;	
	private Regenerator regenerator;

	public RegenPlugin() {
		super();
		plugin = this;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		getLogger().info(this.getName() + "onEnable has been invoked!");
		
		// This will throw a NullPointException if you don't have the command defined in your plugin.yml file!
		getCommand("regen").setExecutor(new PluginCommandExecutor(this));

		//this.getServer().getPluginManager().registerEvents(this, this);
		
		World world = getServer().getWorld("world");
		regenerator = new Regenerator(world);
		regenerator.onEnable();

		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			@Override
			public void run() {
				getServer().getWorld("world").setFullTime(6000);
			}			
		}, 0L, 20L*60L);
		StartRegen(DEFAULT_TASK_PERIOD);
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
		getLogger().info(this.getName() + "onDisable has been invoked!");
		
		//HandlerList.unregisterAll((Listener)this);
		
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
	public void onExplosionPrimeEvent(ExplosionPrimeEvent event){
		
		getLogger().info(event.getEventName());
		event.setRadius(5 * event.getRadius());
	}

	public void RegenAll() {
		regenerator.regenAll();		
	}

	public void TestFunc() {
		getLogger().info(this.getName() + ".TestFunc()");
		World world = getServer().getWorld("world");
		List<BlockPopulator> list = world.getPopulators();
		for (BlockPopulator blockPopulator : list) {
			getLogger().info(" BlockPopulator: " + blockPopulator.toString());			
		}
	}
	
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
