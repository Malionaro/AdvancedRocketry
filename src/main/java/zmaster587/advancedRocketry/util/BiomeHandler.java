package zmaster587.advancedRocketry.util;

import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import zmaster587.advancedRocketry.AdvancedRocketry;

import java.lang.reflect.Field;

/**
 * Server-side helpers for changing 1.16's three-dimensional chunk biome data.
 */
public final class BiomeHandler {

	private static final int HORIZONTAL_MASK = 3;
	private static final int VERTICAL_MASK = 63;
	private static final int WIDTH_BITS = 2;
	private static final Field BIOMES_FIELD = ObfuscationReflectionHelper.findField(BiomeContainer.class, "field_227054_f_");

	static {
		BIOMES_FIELD.setAccessible(true);
	}

	private BiomeHandler() {
	}

	/**
	 * Changes the complete vertical biome column containing {@code pos}.
	 *
	 * <p>Minecraft 1.16 stores biomes in 4x4x4 cells. Setting all 64 vertical
	 * cells preserves the column-based behavior that AdvancedRocketry had in
	 * 1.12.2.</p>
	 *
	 * @return the changed chunk, or {@code null} if no data changed
	 */
	public static Chunk changeBiome(World world, Biome biome, BlockPos pos) {
		if(world.isRemote || biome == null)
			return null;

		Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		Biome[] biomes;
		try {
			biomes = (Biome[]) BIOMES_FIELD.get(chunk.getBiomes());
		}
		catch(IllegalAccessException exception) {
			AdvancedRocketry.logger.error("Unable to access the chunk biome palette", exception);
			return null;
		}

		int quartX = (pos.getX() >> 2) & HORIZONTAL_MASK;
		int quartZ = (pos.getZ() >> 2) & HORIZONTAL_MASK;
		boolean changed = false;

		for(int quartY = 0; quartY <= VERTICAL_MASK; quartY++) {
			int index = (quartY << (WIDTH_BITS + WIDTH_BITS))
					| (quartZ << WIDTH_BITS)
					| quartX;
			if(biomes[index] != biome) {
				biomes[index] = biome;
				changed = true;
			}
		}

		if(changed)
			chunk.setModified(true);
		return changed ? chunk : null;
	}

	/**
	 * Sends the updated biome container to every client tracking the chunk.
	 */
	public static void sendBiomeUpdate(ServerWorld world, Chunk chunk) {
		SChunkDataPacket packet = new SChunkDataPacket(chunk, 0xFFFF);
		world.getChunkProvider().chunkManager.getTrackingPlayers(chunk.getPos(), false)
				.forEach(player -> player.connection.sendPacket(packet));
	}
}
