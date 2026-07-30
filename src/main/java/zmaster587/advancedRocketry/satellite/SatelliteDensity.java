package zmaster587.advancedRocketry.satellite;

import zmaster587.advancedRocketry.api.DataStorage;

/**
 * Collects atmospheric-density data.
 *
 * <p>This satellite type existed in 1.12.2. Keeping its original registry key
 * also allows 1.12.2 satellite NBT with {@code dataType=density} to be loaded
 * during world migration.</p>
 */
public class SatelliteDensity extends SatelliteData {

	public SatelliteDensity() {
		super();
		data = new DataStorage(DataStorage.DataType.ATMOSPHEREDENSITY);
		data.lockDataType(DataStorage.DataType.ATMOSPHEREDENSITY);
	}

	@Override
	public String getName() {
		return "Density Scanner";
	}

	@Override
	public double failureChance() {
		return 0;
	}
}
