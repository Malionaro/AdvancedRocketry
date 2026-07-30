package zmaster587.advancedRocketry.recipe;

import zmaster587.advancedRocketry.tile.multiblock.machine.TilePrecisionLaserEtcher;
import zmaster587.libVulpes.recipe.RecipeMachineFactory;

public class RecipePrecisionLaserEtcher extends RecipeMachineFactory {

	public static final RecipePrecisionLaserEtcher INSTANCE = new RecipePrecisionLaserEtcher();

	@Override
	public Class getMachine() {
		return TilePrecisionLaserEtcher.class;
	}
}
