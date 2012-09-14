package com.gmail.ddmytriw.RegenPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

public class EmptyChunkGenerator extends ChunkGenerator {

	@Override
	public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
		return new short[world.getMaxHeight() / 16][]; //All null (air)
	}

	@Override
	public byte[][] generateBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
		return new byte[world.getMaxHeight() / 16][]; //All null (air)
	}

	@Override
	public boolean canSpawn(World world, int x, int z) {
		return false;
	}

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return new ArrayList<BlockPopulator>();
	}

	@Override
	public Location getFixedSpawnLocation(World world, Random random) {
		return null;
	}
}

