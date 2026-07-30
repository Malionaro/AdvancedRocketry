package zmaster587.advancedRocketry.util;

import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.Constants;
import zmaster587.advancedRocketry.recipe.RecipeLathe;
import zmaster587.advancedRocketry.recipe.RecipeRollingMachine;
import zmaster587.advancedRocketry.tile.multiblock.machine.TileLathe;
import zmaster587.advancedRocketry.tile.multiblock.machine.TileRollingMachine;
import zmaster587.libVulpes.api.material.AllowedProducts;
import zmaster587.libVulpes.api.material.Material;
import zmaster587.libVulpes.api.material.MaterialRegistry;
import zmaster587.libVulpes.interfaces.IRecipe;
import zmaster587.libVulpes.recipe.RecipesMachine;

import java.util.*;

public class ARRecipeHandler {

	private static final String EXTERNAL_RECIPE_PREFIX = "legacy_external/";
	private List<Class<?>> machineList = new ArrayList<>();
	
	public void registerMachine(Class<?> clazz) {
		if(!machineList.contains(clazz)) {
			machineList.add(clazz);
			RecipesMachine.getInstance().recipeList.put(clazz, new LinkedList<>());
		}
		
	}

	/**
	 * Restores the 1.12 OreDictionary integration using the equivalent 1.16
	 * Forge item tags. Unknown materials supplied by other mods receive:
	 * forge:ingots/&lt;material&gt; -> forge:plates/&lt;material&gt; in the
	 * rolling machine, and forge:ingots/&lt;material&gt; ->
	 * forge:rods/&lt;material&gt; in the lathe.
	 */
	public void registerExternalModMaterialRecipes() {
		removeExternalRecipes(TileRollingMachine.class);
		removeExternalRecipes(TileLathe.class);

		if(!ARConfiguration.getCurrentConfig().allowMakingItemsForOtherMods.get())
			return;

		ITagCollection<Item> tags = ItemTags.getCollection();
		List<ResourceLocation> registeredTags = new ArrayList<>(tags.getRegisteredTags());
		registeredTags.sort(Comparator.comparing(ResourceLocation::toString));

		for(ResourceLocation outputTag : registeredTags) {
			if(!"forge".equals(outputTag.getNamespace()))
				continue;

			String path = outputTag.getPath();
			if(path.startsWith("plates/"))
				addExternalPlateRecipe(tags, outputTag, path.substring("plates/".length()));
			else if(path.startsWith("rods/"))
				addExternalRodRecipe(tags, outputTag, path.substring("rods/".length()));
		}
	}

	private void addExternalPlateRecipe(ITagCollection<Item> tags, ResourceLocation outputTag, String materialName) {
		AllowedProducts plates = AllowedProducts.getProductByName("PLATE");
		if(materialName.isEmpty() || materialProvidesProduct(materialName, plates))
			return;

		ResourceLocation inputTag = new ResourceLocation("forge", "ingots/" + materialName);
		ItemStack output = firstStack(tags, outputTag, 1);
		if(output.isEmpty() || !hasItems(tags, inputTag))
			return;

		RecipesMachine.getInstance().addRecipe(
				new ResourceLocation(Constants.modId, EXTERNAL_RECIPE_PREFIX + "rolling/" + materialName),
				RecipeRollingMachine.INSTANCE,
				TileRollingMachine.class,
				output,
				300,
				20,
				inputTag,
				new FluidStack(Fluids.WATER, 100));
	}

	private void addExternalRodRecipe(ITagCollection<Item> tags, ResourceLocation outputTag, String materialName) {
		AllowedProducts rods = AllowedProducts.getProductByName("ROD");
		if(materialName.isEmpty() || materialProvidesProduct(materialName, rods))
			return;

		ResourceLocation inputTag = new ResourceLocation("forge", "ingots/" + materialName);
		ItemStack output = firstStack(tags, outputTag, 2);
		if(output.isEmpty() || !hasItems(tags, inputTag))
			return;

		RecipesMachine.getInstance().addRecipe(
				new ResourceLocation(Constants.modId, EXTERNAL_RECIPE_PREFIX + "lathe/" + materialName),
				RecipeLathe.INSTANCE,
				TileLathe.class,
				output,
				300,
				20,
				inputTag);
	}

	private boolean materialProvidesProduct(String materialName, AllowedProducts product) {
		Material material = MaterialRegistry.getMaterialFromName(materialName);
		return material != null && product != null && product.isOfType(material.getAllowedProducts());
	}

	private boolean hasItems(ITagCollection<Item> tags, ResourceLocation tagName) {
		return tags.getRegisteredTags().contains(tagName)
				&& !tags.getTagByID(tagName).getAllElements().isEmpty();
	}

	private ItemStack firstStack(ITagCollection<Item> tags, ResourceLocation tagName, int count) {
		if(!hasItems(tags, tagName))
			return ItemStack.EMPTY;

		ITag<Item> tag = tags.getTagByID(tagName);
		Optional<Item> first = tag.getAllElements().stream()
				.filter(item -> ForgeRegistries.ITEMS.getKey(item) != null)
				.min(Comparator.comparing(item -> ForgeRegistries.ITEMS.getKey(item).toString()));
		return first.map(item -> new ItemStack(item, count)).orElse(ItemStack.EMPTY);
	}

	private void removeExternalRecipes(Class<?> machine) {
		List<IRecipe> recipes = RecipesMachine.getInstance().getRecipes(machine);
		if(recipes != null)
			recipes.removeIf(recipe -> Constants.modId.equals(recipe.getId().getNamespace())
					&& recipe.getId().getPath().startsWith(EXTERNAL_RECIPE_PREFIX));
	}
}
