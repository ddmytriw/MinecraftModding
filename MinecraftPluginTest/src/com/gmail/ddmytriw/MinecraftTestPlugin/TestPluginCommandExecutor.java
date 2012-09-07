package com.gmail.ddmytriw.MinecraftTestPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestPluginCommandExecutor implements CommandExecutor {
	private MinecraftTestPlugin plugin; // pointer to your main class, unrequired if you don't need methods from the main class
	 
	public TestPluginCommandExecutor(MinecraftTestPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2, String[] arg3) {	
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
	
		if (command.getName().equalsIgnoreCase("testplugin")){ // If the player typed /basic then do the following...
			plugin.getLogger().info(plugin.getName() + " command: testplugin");
			for(int i = 0; i < arg3.length; i++){
				plugin.getLogger().info(plugin.getName() + " 	args: " + arg3[i]);
			}
			
			if(arg3.length > 1){
				plugin.PrintBlockList();
			}
			
			return true;
		} else if (command.getName().equalsIgnoreCase("testplugin2")) {
			if (player == null) {
				sender.sendMessage("this command can only be run by a player");
			} else {
				// do something else...
			}
			return true;
		}
		return false;
	}

}
