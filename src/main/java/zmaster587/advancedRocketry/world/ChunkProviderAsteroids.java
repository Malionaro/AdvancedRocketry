package zmaster587.advancedRocketry.world;

import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.DimensionSettings;
import zmaster587.advancedRocketry.dimension.DimensionProperties;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * Compatibility adapter for code that still constructs the old, dedicated
 * asteroid generator. Live 1.16 dimensions use {@link ChunkProviderPlanet}'s
 * codec and select asteroid terrain through {@link DimensionProperties}.
 */
@Deprecated
public class ChunkProviderAsteroids extends ChunkProviderPlanet {

	public ChunkProviderAsteroids(BiomeProvider biomeProvider, long seed,
			Supplier<DimensionSettings> settings, DimensionProperties properties) {
		this(biomeProvider, biomeProvider, seed, settings, properties);
	}

	public ChunkProviderAsteroids(BiomeProvider biomeProvider, BiomeProvider populationBiomeProvider,
			long seed, Supplier<DimensionSettings> settings, DimensionProperties properties) {
		super(biomeProvider, seed, settings, Collections.emptyList(), properties.getId());
	}
}
