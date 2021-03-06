package com.gmail.ddmytriw.RegenPlugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PluginCommandExecutor implements CommandExecutor {
	private RegenPlugin plugin; // pointer to your main class, unrequired if you don't need methods from the main class
	 
	public PluginCommandExecutor(RegenPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) {	
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
	
		if (command.getName().equalsIgnoreCase("regen")){ // If the player typed /basic then do the following...
			plugin.getLogger().info(plugin.getName() + " command: regen");
			
			if(arg3.length > 0)
			{
				if(arg3[0].equalsIgnoreCase("start"))
				{
					if(arg3.length > 1)
					{
						plugin.StartRegen(Long.valueOf(arg3[1]));
					}
					else
					{
						plugin.StartRegen(plugin.DEFAULT_TASK_PERIOD);						
					}
				}
				else if(arg3[0].equalsIgnoreCase("stop"))
				{
					plugin.StopRegen();
				}
				else if(arg3[0].equalsIgnoreCase("regenall"))
				{
					plugin.RegenAll();
				}
				else if(arg3[0].equalsIgnoreCase("world") && player != null)
				{
					if(arg3.length > 1)
					{
						String world_name = arg3[1];
						Location location = player.getLocation();
						World target_world = plugin.getServer().getWorld(world_name);
						if(target_world != null){
							location.setWorld(target_world);
							boolean result = player.teleport(location);
							assert(result);
							plugin.getLogger().info(plugin.getName() + "Teleporting player to world: " + world_name);
						}
					}
				}
				else if(arg3[0].equalsIgnoreCase("test"))
				{
					plugin.TestFunc();
				}
			}
			return true;
		}
		return false;
	}

}
