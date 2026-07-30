package zmaster587.libVulpes.block.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import zmaster587.libVulpes.api.LibVulpesItems;
import zmaster587.libVulpes.api.LibVulpesTileEntityTypes;

import java.util.Collections;
import java.util.List;

/**
 * Compatibility carrier for the single metadata-based hatch block from 1.12.
 */
public class BlockLegacyHatch extends BlockHatch {

	public static final IntegerProperty VARIANT = IntegerProperty.create("varient", 0, 15);

	public BlockLegacyHatch(Properties properties) {
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
			case 0: return LibVulpesTileEntityTypes.TILE_INPUT_HATCH.create();
			case 1: return LibVulpesTileEntityTypes.TILE_OUTPUT_HATCH.create();
			case 2: return LibVulpesTileEntityTypes.TILE_FLUID_INPUT_HATCH.create();
			case 3: return LibVulpesTileEntityTypes.TILE_FLUID_OUTPUT_HATCH.create();
			default: return null;
		}
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		Item target;
		switch (state.get(VARIANT) & 7) {
			case 0: target = LibVulpesItems.itemItemInputHatch; break;
			case 1: target = LibVulpesItems.itemItemOutputHatch; break;
			case 2: target = LibVulpesItems.itemFluidInputHatch; break;
			case 3: target = LibVulpesItems.itemFluidOutputHatch; break;
			default: return Collections.emptyList();
		}
		return Collections.singletonList(new ItemStack(target));
	}
}
