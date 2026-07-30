package zmaster587.advancedRocketry.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import zmaster587.advancedRocketry.block.BlockLegacyLoader;

import javax.annotation.Nullable;

public class LegacyLoaderBlockItem extends BlockItem {

	public LegacyLoaderBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	@Nullable
	protected BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state == null ? null : state.with(BlockLegacyLoader.VARIANT,
				Math.min(context.getItem().getDamage(), 15));
	}
}
