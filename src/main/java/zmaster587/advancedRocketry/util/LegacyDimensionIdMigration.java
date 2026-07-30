package zmaster587.advancedRocketry.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.Dimension;
import net.minecraftforge.common.util.Constants.NBT;
import zmaster587.advancedRocketry.api.Constants;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.stations.SpaceObjectManager;

import javax.annotation.Nullable;

/**
 * Reads dimension identifiers written by both AdvancedRocketry 1.12.2 and 1.16.5.
 *
 * <p>1.12.2 stored raw integer dimension IDs in many NBT structures. 1.16.5 uses
 * namespaced {@link ResourceLocation}s instead. Keeping the conversion in one
 * place prevents individual items, entities, and satellites from interpreting
 * the same legacy ID differently.</p>
 */
public final class LegacyDimensionIdMigration {

	private LegacyDimensionIdMigration() {
	}

	@Nullable
	public static ResourceLocation read(CompoundNBT nbt, String key) {
		if(nbt.contains(key, NBT.TAG_STRING))
			return ResourceLocation.tryCreate(nbt.getString(key));
		if(nbt.contains(key, NBT.TAG_ANY_NUMERIC))
			return fromLegacyId(nbt.getInt(key));
		return null;
	}

	public static ResourceLocation fromLegacyId(int id) {
		if(id == Integer.MIN_VALUE)
			return SpaceObjectManager.WARPDIMID;
		if(id == Integer.MIN_VALUE + 1)
			return Constants.INVALID_PLANET;
		if(id == -2)
			return DimensionManager.spaceId;
		if(id == -1)
			return Dimension.THE_NETHER.getLocation();
		if(id == 0)
			return Dimension.OVERWORLD.getLocation();
		if(id == 1)
			return Dimension.THE_END.getLocation();
		return new ResourceLocation(Constants.PLANET_NAMESPACE, "planet-" + id);
	}

	public static ResourceLocation fromLegacyStarId(int id) {
		return new ResourceLocation(Constants.STAR_NAMESPACE, Integer.toString(id));
	}

	public static ResourceLocation fromLegacyStationId(int id) {
		return new ResourceLocation(SpaceObjectManager.STATION_NAMESPACE, Integer.toString(id));
	}
}
