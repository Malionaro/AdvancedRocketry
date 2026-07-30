package zmaster587.advancedRocketry.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;

import javax.annotation.Nullable;

/**
 * Long-burning thermite fuel restored from the 1.12 item.
 */
public class ItemThermite extends Item {

	public ItemThermite(Properties properties) {
		super(properties);
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable IRecipeType<?> recipeType) {
		return 6000;
	}
}
