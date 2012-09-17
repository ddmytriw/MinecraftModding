package com.gmail.ddmytriw.RegenPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.generator.BlockPopulator;

public class RegenableBlockPopulator extends BlockPopulator {
	static final List<Material> plantList = Arrays.asList(
			Material.CACTUS
			, Material.SAPLING
			, Material.WOOD
			, Material.LEAVES
			, Material.GRASS
			, Material.WEB
			, Material.DEAD_BUSH
			, Material.RED_ROSE
			, Material.YELLOW_FLOWER
			, Material.WHEAT //crops??
			, Material.CROPS
			, Material.MOB_SPAWNER
			, Material.CHEST
			, Material.LOG
			, Material.RED_MUSHROOM
			, Material.BROWN_MUSHROOM
			, Material.SUGAR_CANE_BLOCK
			, Material.ICE //TODO: Test ice block regen
			, Material.RAILS
			, Material.PUMPKIN
			, Material.HUGE_MUSHROOM_1
			, Material.HUGE_MUSHROOM_2
			, Material.MELON_BLOCK
			, Material.VINE
			, Material.WATER_LILY
			, Material.NETHER_WARTS
			);
	
	public static final List<Material> regenableList = Arrays.asList(
			Material.DIRT
			, Material.STONE
			, Material.GRASS
			, Material.SAND
			, Material.GRAVEL
			, Material.WATER //TODO: Test Water regeneration
			, Material.STATIONARY_WATER
			, Material.LAVA
			, Material.ICE //TODO: Test ice block regeneration
			, Material.GOLD_ORE
			, Material.IRON_ORE
			, Material.COAL_ORE
			, Material.LAPIS_ORE
			, Material.SANDSTONE
			, Material.LAPIS_ORE
			, Material.MOSSY_COBBLESTONE //TODO: Test as part of generateStructures flag
			//, Material.OBSIDIAN //TODO: does obsidian actually spawn naturally??
			, Material.DIAMOND_ORE
			, Material.REDSTONE_ORE
			, Material.GLOWING_REDSTONE_ORE
			, Material.CLAY
			, Material.NETHERRACK
			, Material.SOUL_SAND
			, Material.GLOWSTONE
			, Material.EMERALD_ORE
			);
	
	@Override
	public void populate(World world, Random random, Chunk source) { //TODO: BlockPopulator isnt working
		for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < world.getMaxHeight(); y++) {
                	Block block = source.getBlock(x, y, z);
                	if(!regenableList.contains(block.getType())){
                		block.setType(Material.AIR);
                	}
                	
                	//this should fix some funny stuff
//                	if(block.getType().getId() == Material.ICE.getId()){
//                		block.setType(Material.WATER);
//                	}
                }
            }
        }
		
		Entity[] entities = source.getEntities();
		for (Entity entity : entities) {		 
			if (entity instanceof Item){//make sure we aren't deleting mobs/players
				entity.remove();//remove it
			}
		}
	}

}
