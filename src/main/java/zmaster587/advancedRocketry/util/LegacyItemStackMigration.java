package zmaster587.advancedRocketry.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts the metadata-based items used by Advanced Rocketry 1.12.2 to the
 * individual registry entries used by the 1.16.5 port.
 */
public final class LegacyItemStackMigration {

	private static final Map<String, java.util.List<String>> TARGETS;
	private static final Map<String, java.util.List<String>> LIBVULPES_TARGETS;

	static {
		Map<String, java.util.List<String>> targets = new HashMap<>();
		targets.put("itemcircuitplate", Arrays.asList("basiccircuitwafer", "advancedcircuitwafer"));
		targets.put("ic", Arrays.asList("basiccircuit", "trackingcircuit", "advancedcircuit",
				"controlcircuit", "itemiocircuit", "fluidiocircuit"));
		targets.put("misc", Arrays.asList("userinterface", "carbonbrick"));
		targets.put("itemupgrade", Arrays.asList("hoverupgrade", "flightspeedupgrade",
				"bioniclegsupgrade", "paddedbootsupgrade", "antifogvisorupgrade",
				"earthbrightvisorupgrade"));
		targets.put("pressuretank", Arrays.asList("ironpressuretank", "steelpressuretank",
				"aluminumpressuretank", "titaniumpressuretank"));
		targets.put("satellitepowersource", Arrays.asList("basicsolarpanel", "largesolarpanel"));
		targets.put("satelliteprimaryfunction", Arrays.asList("opticalsensor", "compositionsensor",
				"masssensor", "microwavetransmitter", "oresensor", "biomechangercomponent"));
		TARGETS = Collections.unmodifiableMap(targets);

		Map<String, java.util.List<String>> libVulpesTargets = new HashMap<>();
		libVulpesTargets.put("productdust", Arrays.asList("dustdilithium", "dustiron", "dustgold",
				"dustsilicon", "dustcopper", "dusttin", "duststeel", "dusttitanium", null,
				"dustaluminum", "dustiridium"));
		libVulpesTargets.put("productingot", Arrays.asList(null, null, null, "ingotsilicon",
				"ingotcopper", "ingottin", "ingotsteel", "ingottitanium", null,
				"ingotaluminum", "ingotiridium"));
		libVulpesTargets.put("productgem", Arrays.asList("gemdilithium", null, null, null, null,
				null, null, null, null, null, null));
		libVulpesTargets.put("productboule", Arrays.asList(null, null, null, "boulesilicon", null,
				null, null, null, null, null, null));
		libVulpesTargets.put("productnugget", Arrays.asList(null, null, null, "nuggetsilicon",
				"nuggetcopper", "nuggettin", "nuggetsteel", "nuggettitanium", null,
				"nuggetaluminum", "nuggetiridium"));
		libVulpesTargets.put("productplate", Arrays.asList(null, "plateiron", "plategold",
				"platesilicon", "platecopper", "platetin", "platesteel", "platetitanium",
				null, "platealuminum", "plateiridium"));
		libVulpesTargets.put("productstick", Arrays.asList(null, "rodiron", null, null,
				"rodcopper", null, "rodsteel", "rodtitanium", null, null, "rodiridium"));
		libVulpesTargets.put("productfan", Arrays.asList(null, null, null, null, null, null,
				"fansteel", null, null, null, null));
		libVulpesTargets.put("productsheet", Arrays.asList(null, "sheetiron", null, null,
				"sheetcopper", null, "sheetsteel", "sheettitanium", null, "sheetaluminum", null));
		libVulpesTargets.put("productgear", Arrays.asList(null, null, null, null, null, null,
				"gearsteel", "geartitanium", null, null, null));
		libVulpesTargets.put("ore0", Arrays.asList("oredilithium", null, null, null, "orecopper",
				"oretin", null, null, "orerutile", "orealuminum", "oreiridium"));
		libVulpesTargets.put("metal0", Arrays.asList(null, null, null, null, "blockcopper",
				"blocktin", "blocksteel", "blocktitanium", null, "blockaluminum", "blockiridium"));
		libVulpesTargets.put("coil0", Arrays.asList(null, null, "coilgold", null, "coilcopper",
				null, null, "coiltitanium", null, "coilaluminum", "coiliridium"));
		libVulpesTargets.put("hatch", Arrays.asList("itemihatch", "itemohatch", "fluidihatch",
				"fluidohatch", null, null, null, null, "itemihatch", "itemohatch",
				"fluidihatch", "fluidohatch"));
		LIBVULPES_TARGETS = Collections.unmodifiableMap(libVulpesTargets);
	}

	private LegacyItemStackMigration() {
	}

	public static ItemStack migrate(ItemStack stack) {
		if(stack.isEmpty())
			return stack;

		if(stack.hasTag())
			migrateNestedItemStacks(stack.getTag());

		ResourceLocation registryName = stack.getItem().getRegistryName();
		if(registryName == null)
			return stack;

		ResourceLocation target = getTarget(registryName, stack.getDamage());
		if(target == null)
			return stack;

		CompoundNBT serialized = stack.write(new CompoundNBT());
		rewriteItemStack(serialized, target);
		return ItemStack.read(serialized);
	}

	public static void migrateInventory(IInventory inventory) {
		for(int slot = 0; slot < inventory.getSizeInventory(); slot++) {
			ItemStack original = inventory.getStackInSlot(slot);
			ItemStack migrated = migrate(original);
			if(migrated != original)
				inventory.setInventorySlotContents(slot, migrated);
		}
	}

	/**
	 * Rewrites serialized ItemStacks embedded in another item's tag, such as
	 * spacesuit modules. The compounds are changed before ItemStack.read sees
	 * them, so no component data is discarded.
	 */
	public static void migrateNestedItemStacks(CompoundNBT root) {
		migrateTag(root);
	}

	private static void migrateTag(INBT nbt) {
		if(nbt instanceof CompoundNBT) {
			CompoundNBT compound = (CompoundNBT)nbt;
			if(compound.contains("id", Constants.NBT.TAG_STRING)
					&& compound.contains("Count", Constants.NBT.TAG_ANY_NUMERIC)) {
				ResourceLocation id = ResourceLocation.tryCreate(compound.getString("id"));
				if(id != null) {
					int damage = compound.contains("Damage", Constants.NBT.TAG_ANY_NUMERIC)
							? compound.getInt("Damage")
							: compound.contains("tag", Constants.NBT.TAG_COMPOUND)
									? compound.getCompound("tag").getInt("Damage") : 0;
					ResourceLocation target = getTarget(id, damage);
					if(target != null)
						rewriteItemStack(compound, target);
				}
			}

			for(String key : compound.keySet()) {
				INBT child = compound.get(key);
				if(child != null)
					migrateTag(child);
			}
		}
		else if(nbt instanceof ListNBT) {
			for(INBT child : (ListNBT)nbt)
				migrateTag(child);
		}
	}

	private static ResourceLocation getTarget(ResourceLocation oldId, int damage) {
		java.util.List<String> targets;
		if(zmaster587.advancedRocketry.api.Constants.modId.equals(oldId.getNamespace()))
			targets = TARGETS.get(oldId.getPath());
		else if("libvulpes".equals(oldId.getNamespace())) {
			if("battery".equals(oldId.getPath()))
				return damage == 1 ? new ResourceLocation("libvulpes", "batterypack") : null;
			targets = LIBVULPES_TARGETS.get(oldId.getPath());
		}
		else
			return null;

		String target = targets != null && damage >= 0 && damage < targets.size()
				? targets.get(damage) : null;
		return target == null ? null : new ResourceLocation(oldId.getNamespace(), target);
	}

	private static void rewriteItemStack(CompoundNBT itemStack, ResourceLocation target) {
		itemStack.putString("id", target.toString());
		itemStack.remove("Damage");
		if(itemStack.contains("tag", Constants.NBT.TAG_COMPOUND)) {
			CompoundNBT tag = itemStack.getCompound("tag");
			tag.remove("Damage");
			if(tag.isEmpty())
				itemStack.remove("tag");
		}
	}
}
