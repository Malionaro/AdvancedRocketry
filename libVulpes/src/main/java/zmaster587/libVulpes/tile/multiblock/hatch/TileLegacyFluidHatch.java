package zmaster587.libVulpes.tile.multiblock.hatch;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;
import zmaster587.libVulpes.api.LibVulpesTileEntityTypes;
import zmaster587.libVulpes.block.multiblock.BlockLegacyHatch;

/**
 * Loads the single 1.12 fluid-hatch tile ID while retaining whether that tile
 * was an input or output hatch.
 */
public class TileLegacyFluidHatch extends TileFluidHatch {

	private boolean outputOnly;

	public TileLegacyFluidHatch() {
		super(LibVulpesTileEntityTypes.TILE_LEGACY_FLUID_HATCH);
	}

	@Override
	public boolean isOutputOnly() {
		return outputOnly;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		return outputOnly ? 0 : super.fill(resource, action);
	}

	@Override
	public String getModularInventoryName() {
		return outputOnly ? "block.libvulpes.fluidohatch" : "block.libvulpes.fluidihatch";
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		nbt.putBoolean("outputOnly", outputOnly);
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if(nbt.contains("outputOnly"))
			outputOnly = nbt.getBoolean("outputOnly");
		else if(state.hasProperty(BlockLegacyHatch.VARIANT))
			outputOnly = (state.get(BlockLegacyHatch.VARIANT) & 7) == 3;
	}
}
