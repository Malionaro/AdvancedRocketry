package zmaster587.advancedRocketry.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.util.DimensionBlockPosition;
import zmaster587.advancedRocketry.util.NBTStorableListList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Stores the space-elevator endpoints selected by the player.
 */
public class ItemSpaceElevatorChip extends Item {

	public ItemSpaceElevatorChip(Properties properties) {
		super(properties);
	}

	public List<DimensionBlockPosition> getBlockPositions(ItemStack stack) {
		NBTStorableListList positions = new NBTStorableListList();
		if (stack.hasTag())
			positions.readFromNBT(stack.getTag());
		return positions.getList();
	}

	public void setBlockPositions(ItemStack stack, List<DimensionBlockPosition> positionsToStore) {
		if (positionsToStore.isEmpty()) {
			if (stack.hasTag())
				stack.getTag().remove("list");
			return;
		}

		CompoundNBT nbt = stack.getOrCreateTag();
		new NBTStorableListList(positionsToStore).writeToNBT(nbt);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		int count = getBlockPositions(stack).size();
		tooltip.add(count == 0
				? new TranslationTextComponent("msg.empty")
				: new TranslationTextComponent("msg.advancedrocketry.elevatorchip.entries", count));
	}
}
