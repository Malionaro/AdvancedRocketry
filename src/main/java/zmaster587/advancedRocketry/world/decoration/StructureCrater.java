package zmaster587.advancedRocketry.world.decoration;

import com.mojang.serialization.Codec;

import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.dimension.DimensionProperties.CraterBiomeWeight;
import zmaster587.advancedRocketry.world.ChunkProviderPlanet;

public class StructureCrater  extends Structure<ProbabilityConfig> {

	public enum CraterType {
		SMALL("crater_small"),
		NORMAL("crater"),
		HUGE("crater_huge");

		private final String registryPath;

		CraterType(String registryPath) {
			this.registryPath = registryPath;
		}
	}

	private final CraterType craterType;

	public StructureCrater(Codec<ProbabilityConfig> p_i231947_1_, CraterType craterType) {
		super(p_i231947_1_);
		this.craterType = craterType;
	}

	public Structure.IStartFactory<ProbabilityConfig> getStartFactory() {
		return StructureCrater.Start::new;
	}

	public static class Start extends StructureStart<ProbabilityConfig> {
		private final CraterType craterType;

		public Start(Structure<ProbabilityConfig> p_i225801_1_, int p_i225801_2_, int p_i225801_3_, MutableBoundingBox p_i225801_4_, int p_i225801_5_, long p_i225801_6_) {
			super(p_i225801_1_, p_i225801_2_, p_i225801_3_, p_i225801_4_, p_i225801_5_, p_i225801_6_);
			this.craterType = ((StructureCrater)p_i225801_1_).craterType;
		}

		public void func_230364_a_(DynamicRegistries p_230364_1_, ChunkGenerator p_230364_2_, TemplateManager p_230364_3_, int p_230364_4_, int p_230364_5_, Biome p_230364_6_, ProbabilityConfig p_230364_7_) {
			DimensionProperties properties = p_230364_2_ instanceof ChunkProviderPlanet
					? ((ChunkProviderPlanet)p_230364_2_).getDimensionProperties()
					: null;
			if(properties != null && !canGenerateInBiome(properties, p_230364_6_, this.rand.nextInt(100)))
				return;

			boolean airless = properties != null && properties.getAtmosphereDensity() <= 5;
			StructurePieceCrater craterPiece = new StructurePieceCrater(this.rand, p_230364_4_ * 16, p_230364_5_ * 16, craterType, airless);
			this.components.add(craterPiece);
			this.recalculateStructureSize();
		}

		private static boolean canGenerateInBiome(DimensionProperties properties, Biome biome, int roll) {
			if(properties.getCraterBiomeWeights().isEmpty())
				return true;

			for(CraterBiomeWeight entry : properties.getCraterBiomeWeights()) {
				if(entry.getBiomeId().equals(biome.getRegistryName()) && entry.getWeight() > roll)
					return true;
			}
			return false;
		}
	}
	
	@Override
	public String getStructureName() {
		return "advancedrocketry:" + craterType.registryPath;
	}

	public GenerationStage.Decoration func_236396_f_() {
		return GenerationStage.Decoration.LOCAL_MODIFICATIONS;
	}
}
