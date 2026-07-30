package zmaster587.libVulpes.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.reflect.FieldUtils;

import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraft.world.gen.placement.TopSolidRangeConfig;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.config.LibVulpesConfig;
import zmaster587.libVulpes.api.material.AllowedProducts;
import zmaster587.libVulpes.api.material.MaterialRegistry;

public class OreGen {

	public static final ConfiguredFeature<?, ?> COPPER_ORE = createOre("ore_copper", "copper",
			LibVulpesConfig.getCurrentConfig().copperClumpSize.get(),
			LibVulpesConfig.getCurrentConfig().copperMinHeight.get(),
			LibVulpesConfig.getCurrentConfig().copperMaxHeight.get(),
			LibVulpesConfig.getCurrentConfig().copperPerChunk.get());
	public static final ConfiguredFeature<?, ?> TIN_ORE = createOre("ore_tin", "tin",
			LibVulpesConfig.getCurrentConfig().tinClumpSize.get(),
			LibVulpesConfig.getCurrentConfig().tinMinHeight.get(),
			LibVulpesConfig.getCurrentConfig().tinMaxHeight.get(),
			LibVulpesConfig.getCurrentConfig().tinPerChunk.get());
	public static final ConfiguredFeature<?, ?> RUTILE_ORE = createOre("ore_rutile", "rutile",
			LibVulpesConfig.getCurrentConfig().rutileClumpSize.get(),
			LibVulpesConfig.getCurrentConfig().rutileMinHeight.get(),
			LibVulpesConfig.getCurrentConfig().rutileMaxHeight.get(),
			LibVulpesConfig.getCurrentConfig().rutilePerChunk.get());
	public static final ConfiguredFeature<?, ?> ALUMINUM_ORE = createOre("ore_aluminum", "aluminum",
			LibVulpesConfig.getCurrentConfig().aluminumClumpSize.get(),
			LibVulpesConfig.getCurrentConfig().aluminumMinHeight.get(),
			LibVulpesConfig.getCurrentConfig().aluminumMaxHeight.get(),
			LibVulpesConfig.getCurrentConfig().aluminumPerChunk.get());
	public static final ConfiguredFeature<?, ?> IRIDIUM_ORE = createOre("ore_iridium", "iridium",
			LibVulpesConfig.getCurrentConfig().iridiumClumpSize.get(),
			LibVulpesConfig.getCurrentConfig().iridiumMinHeight.get(),
			LibVulpesConfig.getCurrentConfig().iridiumMaxHeight.get(),
			LibVulpesConfig.getCurrentConfig().iridiumPerChunk.get());
	public static final ConfiguredFeature<?, ?> DILITHIUM_ORE = createOre("ore_dilithium", "dilithium",
			LibVulpesConfig.getCurrentConfig().dilithiumClumpSize.get(),
			LibVulpesConfig.getCurrentConfig().dilithiumMinHeight.get(),
			LibVulpesConfig.getCurrentConfig().dilithiumMaxHeight.get(),
			LibVulpesConfig.getCurrentConfig().dilithiumPerChunk.get());

	private static ConfiguredFeature<?, ?> createOre(String registryName, String materialName,
			int clumpSize, int minHeight, int maxHeight, int veinsPerChunk) {
		int safeMinHeight = Math.max(0, minHeight);
		int safeMaxHeight = Math.max(safeMinHeight + 1, maxHeight);
		ConfiguredFeature<?, ?> feature = Feature.ORE.withConfiguration(new OreFeatureConfig(
				OreFeatureConfig.FillerBlockType.BASE_STONE_OVERWORLD,
				((BlockItem)MaterialRegistry.getMaterialFromName(materialName)
						.getProduct(AllowedProducts.getProductByName("ORE")).getItem())
						.getBlock().getDefaultState(),
				Math.max(1, clumpSize)))
				.withPlacement(Placement.RANGE.configure(
						new TopSolidRangeConfig(safeMinHeight, 0, safeMaxHeight)))
				.square()
				.count(Math.max(0, veinsPerChunk));
		return register(registryName, feature);
	}

	private static <FC extends IFeatureConfig> ConfiguredFeature<FC, ?> register(String p_243968_0_, ConfiguredFeature<FC, ?> p_243968_1_) {
		return Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(LibVulpes.MODID, p_243968_0_), p_243968_1_);
	}

	private static final int UNDERGROUND_ORES = GenerationStage.Decoration.UNDERGROUND_ORES.ordinal();

	public static void injectOreGen() {
		if(!LibVulpesConfig.getCurrentConfig().enableOreGen.get()) {
			return;
		}

		final Field features = ObfuscationReflectionHelper.findField(BiomeGenerationSettings.class, "field_242484_f");
		setMutable(features);

		updateAllFeatures(features);
		ForgeRegistries.BIOMES.iterator().forEachRemaining((biome) -> {
			if(LibVulpesConfig.getCurrentConfig().generateCopper.get())
				getOreFeatures(biome).add(() -> COPPER_ORE);
			if(LibVulpesConfig.getCurrentConfig().generateTin.get())
				getOreFeatures(biome).add(() -> TIN_ORE);
			if(LibVulpesConfig.getCurrentConfig().generateRutile.get())
				getOreFeatures(biome).add(() -> RUTILE_ORE);
			if(LibVulpesConfig.getCurrentConfig().generateAluminum.get())
				getOreFeatures(biome).add(() -> ALUMINUM_ORE);
			if(LibVulpesConfig.getCurrentConfig().generateIridium.get())
				getOreFeatures(biome).add(() -> IRIDIUM_ORE);
			if(LibVulpesConfig.getCurrentConfig().generateDilithium.get())
				getOreFeatures(biome).add(() -> DILITHIUM_ORE);
		});
	}

	/** Replace all feature arrays with mutable copies. */
	private static void updateAllFeatures(Field features) {
		for (Biome b : WorldGenRegistries.BIOME) {
			final BiomeGenerationSettings settings = b.getGenerationSettings();
			final List<List<Supplier<ConfiguredFeature<?, ?>>>> current = getValue(features, settings);
			final List<List<Supplier<ConfiguredFeature<?, ?>>>> values = Collections.synchronizedList(new LinkedList<>());
			current.forEach(list -> values.add(new LinkedList<>(list)));
			setValue(features, settings, values);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getValue(Field f, Object instance) {
		try {
			return (T) f.get(instance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}
	public static void setValue(Field f, Object instance, Object value) {
		try {
			f.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** Retrieves the current set of features in the ore phase. */
	private static List<Supplier<ConfiguredFeature<?, ?>>> getOreFeatures(Biome b) {
		final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = b.getGenerationSettings().getFeatures();
		while (features.size() <= UNDERGROUND_ORES) {
			features.add(new LinkedList<>());
		}
		return features.get(UNDERGROUND_ORES);
	}

	public static void setMutable(Field f) {
		FieldUtils.removeFinalModifier(f, true);
	}
}
