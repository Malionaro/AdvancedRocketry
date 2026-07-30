package zmaster587.advancedRocketry.tile.multiblock.machine;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.AdvancedRocketryFluids;
import zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType;
import zmaster587.advancedRocketry.api.Constants;
import zmaster587.advancedRocketry.inventory.TextureResources;
import zmaster587.advancedRocketry.recipe.RecipeCentrifuge;
import zmaster587.advancedRocketry.util.AudioRegistry;
import zmaster587.libVulpes.api.LibVulpesBlocks;
import zmaster587.libVulpes.block.BlockMeta;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleProgress;
import zmaster587.libVulpes.interfaces.IRecipe;
import zmaster587.libVulpes.recipe.RecipesMachine;
import zmaster587.libVulpes.recipe.RecipesMachine.ChanceFluidStack;
import zmaster587.libVulpes.recipe.RecipesMachine.ChanceItemStack;
import zmaster587.libVulpes.tile.multiblock.TileMultiblockMachine;
import zmaster587.libVulpes.util.ZUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class TileCentrifuge extends TileMultiblockMachine {
	public static final Object[][][] structure = {

			{{Blocks.AIR, Blocks.AIR, Blocks.AIR},
					{LibVulpesBlocks.blockAdvancedMachineStructure, LibVulpesBlocks.blockAdvancedMachineStructure, Blocks.AIR},
					{LibVulpesBlocks.blockAdvancedMachineStructure, LibVulpesBlocks.blockAdvancedMachineStructure, Blocks.AIR}},

			{{Blocks.AIR, Blocks.AIR, Blocks.AIR},
					{LibVulpesBlocks.blockAdvancedMachineStructure, LibVulpesBlocks.blockAdvancedMachineStructure, Blocks.AIR},
					{LibVulpesBlocks.blockAdvancedMachineStructure, LibVulpesBlocks.blockAdvancedMachineStructure, Blocks.AIR}},

			{{'c', Blocks.AIR, Blocks.AIR},
					{LibVulpesBlocks.blockAdvancedMachineStructure, LibVulpesBlocks.blockAdvancedMachineStructure, Blocks.AIR},
					{LibVulpesBlocks.blockAdvancedMachineStructure, LibVulpesBlocks.blockAdvancedMachineStructure, Blocks.AIR}},

			{   {'P','L', 'l'},
				{LibVulpesBlocks.motors,'O', new BlockMeta(LibVulpesBlocks.blockMachineStructure)},
			  {new BlockMeta(LibVulpesBlocks.blockMachineStructure), new BlockMeta(LibVulpesBlocks.blockMachineStructure), 'l'}},

	};

	public TileCentrifuge() {
		super(AdvancedRocketryTileEntityType.TILE_CENTRIFUGE);
	}

	public static void registerEnrichedLavaRecipe() {
		List<ChanceItemStack> outputs = new ArrayList<>();
		for(String entry : ARConfiguration.getCurrentConfig().lavaCentrifugeOutputs.get()) {
			int separator = entry.lastIndexOf(';');
			if(separator <= 0 || separator == entry.length() - 1) {
				AdvancedRocketry.logger.warn("Invalid lavaCentrifugeOutputs entry '{}'; expected item-or-tag;weight", entry);
				continue;
			}

			try {
				float weight = Float.parseFloat(entry.substring(separator + 1).trim());
				List<ItemStack> stacks = ZUtils.readListFromString(entry.substring(0, separator).trim());
				if(weight <= 0 || stacks.isEmpty())
					continue;
				outputs.add(new ChanceItemStack(stacks.get(0).copy(), weight));
			}
			catch(NumberFormatException exception) {
				AdvancedRocketry.logger.warn("Invalid lavaCentrifugeOutputs weight in '{}'", entry);
			}
		}

		if(outputs.isEmpty()) {
			AdvancedRocketry.logger.warn("No valid enriched-lava centrifuge outputs were configured; retaining the datapack recipe");
			return;
		}

		List<FluidStack> fluidInputs = Collections.singletonList(
				new FluidStack(AdvancedRocketryFluids.enrichedLavaStill.get(), 1000));
		List<ChanceFluidStack> fluidOutputs = Collections.singletonList(
				new ChanceFluidStack(new FluidStack(Fluids.LAVA, 1000), 1f));
		ResourceLocation id = new ResourceLocation(Constants.modId, "enrichedlava");
		RecipesMachine.LibVulpesRecipe recipe = new RecipesMachine.LibVulpesRecipe(
				RecipeCentrifuge.INSTANCE,
				id,
				outputs,
				new LinkedList<>(),
				fluidOutputs,
				fluidInputs,
				200,
				10,
				new HashMap<>());
		recipe.setMaxOutputSize(4);

		List<IRecipe> recipes = RecipesMachine.getInstance().getRecipes(TileCentrifuge.class);
		recipes.removeIf(existing -> id.equals(existing.getId()));
		recipes.add(recipe);
	}

	@Override
	public Object[][][] getStructure() {
		return structure;
	}


	@Override
	public boolean shouldHideBlock(World world, BlockPos pos2, BlockState tile) {
		return true;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos.add(-2,-2,-2), pos.add(2,2,2));
	}

	@Override
	public SoundEvent getSound() {
		return AudioRegistry.electrolyser;
	}


	@Override
	public List<ModuleBase> getModules(int ID, PlayerEntity player) {
		List<ModuleBase> modules = super.getModules(ID, player);

		modules.add(new ModuleProgress(100, 4, 0, TextureResources.crystallizerProgressBar, this));
		return modules;
	}

	@Override
	public String getMachineName() {
		return "block.advancedrocketry.centrifuge";
	}
}
