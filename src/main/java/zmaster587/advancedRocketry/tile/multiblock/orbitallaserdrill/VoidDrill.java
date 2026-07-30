package zmaster587.advancedRocketry.tile.multiblock.orbitallaserdrill;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * This drill is used if the laserDrillPlanet config option is disabled. It simply conjures ores from nowhere
 */
class VoidDrill extends AbstractDrill {

	private final Random random;
	private List<ItemStack> ores;
	private boolean planetOresInitialized;

	VoidDrill() {
		this.random = new Random();
		this.planetOresInitialized = false;
		loadGlobalOres();
	}

	private void loadGlobalOres() {
		// Never retain or modify the configuration's mutable list. A drill adds
		// dimension-specific entries when it starts, and those additions must
		// remain local to this drill.
		ores = ARConfiguration.getCurrentConfig().standardLaserDrillOres.stream()
				.map(ItemStack::copy)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Performs a single drilling operation
	 *
	 * @return The ItemStacks produced by this tick of drilling
	 */
	ItemStack[] performOperation() {
		ArrayList<ItemStack> items = new ArrayList<>();
		if (!ores.isEmpty() && random.nextInt(10) == 0) {
			ItemStack item = ores.get(random.nextInt(ores.size()));
			ItemStack newStack = item.copy();
			items.add(newStack);
		} else
			items.add(new ItemStack(Blocks.COBBLESTONE, 5));

		ItemStack[] stacks = new ItemStack[items.size()];

		stacks = items.toArray(stacks);

		return stacks;
	}

	boolean activate(World world, int x, int z) {
		// Ideally, this should be done in the constructor, but the world provider is null there for reasons unknown, so this gets delayed until first activation
		if(!this.planetOresInitialized) {
			DimensionProperties dimProperties = DimensionManager.getInstance().getDimensionProperties(world);
			dimProperties.laserDrillOres.stream()
					.filter(stack -> !ores.contains(stack))
					.map(ItemStack::copy)
					.forEach(ores::add);
			this.planetOresInitialized = true;
		}
		return true;
	}

	void deactivate() {}

	boolean isFinished() {
		return false;
	}

	boolean needsRestart() {
		return false;
	}
}
