package zmaster587.libVulpes.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import zmaster587.libVulpes.api.material.AllowedProducts;
import zmaster587.libVulpes.api.material.MaterialRegistry;

import java.util.Collections;
import java.util.List;

/**
 * State carrier for the metadata-based material blocks used by LibVulpes 1.12.
 * The deliberately misspelled property name matches the original blockstate.
 */
public class BlockLegacyMaterial extends Block {

	public static final IntegerProperty VARIANT = IntegerProperty.create("varient", 0, 15);
	private static final String[] MATERIALS = {
			"dilithium", "iron", "gold", "silicon", "copper", "tin",
			"steel", "titanium", "rutile", "aluminum", "iridium"
	};

	private final String productName;

	public BlockLegacyMaterial(Properties properties, String productName) {
		super(properties);
		this.productName = productName;
		setDefaultState(getDefaultState().with(VARIANT, 0));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(VARIANT);
	}

	public String getProductName() {
		return productName;
	}

	public ItemStack getConvertedStack(BlockState state) {
		int variant = state.get(VARIANT);
		if(variant < 0 || variant >= MATERIALS.length)
			return ItemStack.EMPTY;

		AllowedProducts product = AllowedProducts.getProductByName(productName);
		return product == null
				? ItemStack.EMPTY
				: MaterialRegistry.getItemStackFromMaterialAndType(MATERIALS[variant], product);
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ItemStack converted = getConvertedStack(state);
		return converted.isEmpty() ? Collections.emptyList() : Collections.singletonList(converted);
	}
}
