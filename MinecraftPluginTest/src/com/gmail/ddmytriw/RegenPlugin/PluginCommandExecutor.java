package com.gmail.ddmytriw.RegenPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PluginCommandExecutor implements CommandExecutor {
	private RegenPlugin plugin; // pointer to your main class, unrequired if you don't need methods from the main class
	 
	public PluginCommandExecutor(RegenPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) {	
//		Player player = null;
//		if (sender instanceof Player) {
//			player = (Player) sender;
//		}
	
		if (command.getName().equalsIgnoreCase("regen")){ // If the player typed /basic then do the following...
			plugin.getLogger().info(plugin.getName() + " command: regen");
			
			if(arg3.length > 0)
			{
				if(arg3[0].equalsIgnoreCase("start"))
				{
					plugin.StartRegen();
				}
				else if(arg3[0].equalsIgnoreCase("stop"))
				{
					plugin.StopRegen();
				}
			}
			return true;
		}
		return false;
	}

}
