package com.gmail.ddmytriw.MinecraftTestPlugin;

import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftTestPlugin extends JavaPlugin {

	@Override
	public void onDisable() {
		super.onDisable();
		getLogger().info(this.getName() + "onDisable has been invoked!");
	}

	@Override
	public void onEnable() {
		super.onEnable();
		getLogger().info(this.getName() + "onEnable has been invoked!");
		
		// This will throw a NullPointException if you don't have the command defined in your plugin.yml file!
		getCommand("testplugin").setExecutor(new TestPluginCommandExecutor(this));
	}
}
