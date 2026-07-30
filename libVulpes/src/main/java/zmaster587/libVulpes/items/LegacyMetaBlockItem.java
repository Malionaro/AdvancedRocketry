package zmaster587.libVulpes.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IntegerProperty;

import javax.annotation.Nullable;

/**
 * Preserves the old ItemStack damage value when a legacy metadata carrier is
 * placed before the normal inventory migration has replaced it.
 */
public class LegacyMetaBlockItem extends BlockItem {

	private final IntegerProperty variantProperty;

	public LegacyMetaBlockItem(Block block, Properties properties, IntegerProperty variantProperty) {
		super(block, properties);
		this.variantProperty = variantProperty;
	}

	@Override
	@Nullable
	protected BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = super.getStateForPlacement(context);
		return state == null
				? null
				: state.with(variantProperty, Math.min(context.getItem().getDamage(), 15));
	}
}
