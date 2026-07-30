package zmaster587.advancedRocketry.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType;
import zmaster587.advancedRocketry.block.multiblock.BlockARHatch;

/**
 * Compatibility carrier for the metadata-based 1.12 loader block.
 */
public class BlockLegacyLoader extends BlockARHatch {
	public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 15);

	public BlockLegacyLoader(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(VARIANT, 0));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(VARIANT);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		switch (state.get(VARIANT) & 7) {
			case 0: return AdvancedRocketryTileEntityType.TILE_DATA_BUS.create();
			case 1: return AdvancedRocketryTileEntityType.TILE_SATELLITE_BAY.create();
			case 2: return AdvancedRocketryTileEntityType.TILE_ROCKET_UNLOADER.create();
			case 3: return AdvancedRocketryTileEntityType.TILE_ROCKET_LOADER.create();
			case 4: return AdvancedRocketryTileEntityType.TILE_FLUID_UNLOADER.create();
			case 5: return AdvancedRocketryTileEntityType.TILE_FLUID_LOADER.create();
			case 6: return AdvancedRocketryTileEntityType.TILE_GUIDANCE_COMPUTER__ACCESS_HATCH.create();
			default: return null;
		}
	}
}
