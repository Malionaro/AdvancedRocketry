package zmaster587.advancedRocketry.satellite;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import zmaster587.advancedRocketry.api.AdvancedRocketryItems;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.advancedRocketry.api.satellite.SatelliteProperties;
import zmaster587.advancedRocketry.item.ItemBiomeChanger;
import zmaster587.advancedRocketry.util.BiomeHandler;
import zmaster587.libVulpes.api.IUniversalEnergy;
import zmaster587.libVulpes.util.ZUtils;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class SatelliteBiomeChanger extends SatelliteBase {

	private static final int CHANGE_RADIUS = 16;
	private static final int MAX_QUEUED_BLOCKS = 1024;
	private static final int CHANGES_PER_TICK = 10;
	private static final int ENERGY_PER_CHANGE = 120;

	private ResourceLocation selectedBiome;
	private final Queue<BlockPos> positionsToChange;
	private final Set<ResourceLocation> discoveredBiomes;

	public SatelliteBiomeChanger() {
		positionsToChange = new ArrayDeque<>();
		discoveredBiomes = new HashSet<>();
	}

	public void setBiome(Biome biome) {
		selectedBiome = biome == null ? null : AdvancedRocketryBiomes.getBiomeResource(biome);
	}

	public Biome getBiome() {
		return selectedBiome == null ? null : AdvancedRocketryBiomes.getBiomeFromResourceLocation(selectedBiome);
	}

	public Set<ResourceLocation> getDiscoveredBiomes() {
		return discoveredBiomes;
	}

	public void addBiome(Biome biome) {
		ResourceLocation biomeId = biome == null ? null : AdvancedRocketryBiomes.getBiomeResource(biome);
		if(biomeId != null && !AdvancedRocketryBiomes.instance.getBlackListedBiomes().contains(biomeId)) {
			discoveredBiomes.add(biomeId);
			if(selectedBiome == null)
				selectedBiome = biomeId;
		}
	}

	@Override
	public String getInfo(World world) {
		return selectedBiome == null ? "No biome selected" : "Ready";
	}

	@Override
	public String getName() {
		return "Biome Changer";
	}

	@Override
	@Nonnull
	public ItemStack getControllerItemStack(@Nonnull ItemStack satIdChip, SatelliteProperties properties) {
		ItemStack controller = satIdChip.getItem() instanceof ItemBiomeChanger
				? satIdChip
				: new ItemStack(AdvancedRocketryItems.itemBiomeChangerRemote);
		((ItemBiomeChanger) controller.getItem()).setSatellite(controller, properties);
		return controller;
	}

	@Override
	public boolean isAcceptableControllerItemStack(@Nonnull ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ItemBiomeChanger;
	}

	@Override
	public void tickEntity() {
		if(selectedBiome != null && getDimensionId().isPresent()) {
			ServerWorld world = ZUtils.getWorld(getDimensionId().get());
			Biome biome = getBiome();
			if(world != null && biome != null) {
				Set<Chunk> changedChunks = new HashSet<>();
				for(int i = 0; i < CHANGES_PER_TICK && !positionsToChange.isEmpty(); i++) {
					if(battery.extractEnergy(ENERGY_PER_CHANGE, false) != ENERGY_PER_CHANGE)
						break;

					Chunk changedChunk = BiomeHandler.changeBiome(world, biome, positionsToChange.remove());
					if(changedChunk != null)
						changedChunks.add(changedChunk);
				}
				for(Chunk chunk : changedChunks)
					BiomeHandler.sendBiomeUpdate(world, chunk);
			}
		}
		super.tickEntity();
	}

	@Override
	public boolean performAction(PlayerEntity player, World world, BlockPos pos) {
		if(world.isRemote || selectedBiome == null || !getDimensionId().isPresent()
				|| !getDimensionId().get().equals(ZUtils.getDimensionIdentifier(world)))
			return false;

		for(int x = pos.getX() - CHANGE_RADIUS; x < pos.getX() + CHANGE_RADIUS; x++) {
			for(int z = pos.getZ() - CHANGE_RADIUS; z < pos.getZ() + CHANGE_RADIUS; z++) {
				if(positionsToChange.size() >= MAX_QUEUED_BLOCKS)
					return true;
				positionsToChange.add(new BlockPos(x, 0, z));
			}
		}
		return true;
	}

	@Override
	public double failureChance() {
		return 0;
	}

	public IUniversalEnergy getBattery() {
		return battery;
	}

	@Override
	public void writeToNBT(CompoundNBT nbt) {
		super.writeToNBT(nbt);
		if(selectedBiome != null)
			nbt.putString("biome", selectedBiome.toString());

		int[] positions = new int[positionsToChange.size() * 3];
		int positionIndex = 0;
		for(BlockPos pos : positionsToChange) {
			positions[positionIndex++] = pos.getX();
			positions[positionIndex++] = pos.getY();
			positions[positionIndex++] = pos.getZ();
		}
		nbt.putIntArray("posList", positions);

		ListNBT biomes = new ListNBT();
		for(ResourceLocation biome : discoveredBiomes)
			biomes.add(StringNBT.valueOf(biome.toString()));
		nbt.put("biomes", biomes);
	}

	@Override
	public void readFromNBT(CompoundNBT nbt) {
		super.readFromNBT(nbt);
		if(nbt.contains("biome"))
			selectedBiome = ResourceLocation.tryCreate(nbt.getString("biome"));
		else if(nbt.contains("biomeId"))
			setBiome(AdvancedRocketryBiomes.getBiomeRegistry().getByValue(nbt.getInt("biomeId")));

		positionsToChange.clear();
		int[] positions = nbt.getIntArray("posList");
		for(int i = 0; i + 2 < positions.length; i += 3)
			positionsToChange.add(new BlockPos(positions[i], positions[i + 1], positions[i + 2]));

		discoveredBiomes.clear();
		ListNBT biomes = nbt.getList("biomes", 8);
		for(int i = 0; i < biomes.size(); i++) {
			ResourceLocation biome = ResourceLocation.tryCreate(biomes.getString(i));
			if(biome != null)
				discoveredBiomes.add(biome);
		}

		// 1.12.2 stored discovered biome IDs as an int array.
		if(discoveredBiomes.isEmpty()) {
			for(int biomeId : nbt.getIntArray("biomeList")) {
				Biome biome = AdvancedRocketryBiomes.getBiomeRegistry().getByValue(biomeId & 0xFF);
				if(biome != null)
					discoveredBiomes.add(AdvancedRocketryBiomes.getBiomeResource(biome));
			}
		}
	}
}
