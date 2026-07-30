package zmaster587.libVulpes.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zmaster587.libVulpes.LibVulpes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class LibVulpesConfig {

	private final static String CATEGORY_ORE = "ore_generation";
	private final static String CATEGORY_COPPER = "copper";
	private final static String CATEGORY_TIN = "tin";
	private final static String CATEGORY_DILITHIUM = "dilithium";
	private final static String CATEGORY_ALUMINUM = "aluminum";
	private final static String CATEGORY_RUTILE = "rutile";
	private final static String CATEGORY_IRIDIUM = "iridium";

	//Only to be set in preinit
	private static LibVulpesConfig currentConfig;
	public static Logger logger = LogManager.getLogger(LibVulpes.MODID);

	public static final LibVulpesConfig common;
	private static final ForgeConfigSpec commonSpec;
	
	static {
		Pair<LibVulpesConfig, ForgeConfigSpec> commonConfiguration = new ForgeConfigSpec.Builder().configure(LibVulpesConfig::new);
		common = commonConfiguration.getLeft();
		commonSpec = commonConfiguration.getRight();
	}
	
	public static void register() {
		registerConfig(ModConfig.Type.COMMON, commonSpec, "libvulpes.toml");
	}

	private static void registerConfig(ModConfig.Type type, ForgeConfigSpec spec, String fileName) {
		LibVulpes.MOD_CONTAINER.addConfig(new ModConfig(type, spec, LibVulpes.MOD_CONTAINER, fileName));
	}

	private LibVulpesConfig() { }

	public LibVulpesConfig(ForgeConfigSpec.Builder builder) {
		currentConfig = new LibVulpesConfig();

		LibVulpesConfig lvConfig = getCurrentConfig();

		builder.push(CATEGORY_ORE);
		lvConfig.enableOreGen = builder.comment("Master switch for LibVulpes ore generation.")
				.define("enableOreGen", true);
		builder.push(CATEGORY_COPPER);
		lvConfig.generateCopper = builder.define("generateCopper", true);
		lvConfig.copperClumpSize = builder.defineInRange("sizeCopper", 6, 1, 64);
		lvConfig.copperPerChunk = builder.defineInRange("chanceCopper", 10, 0, 256);
		lvConfig.copperMinHeight = builder.defineInRange("minHeightCopper", 0, 0, 255);
		lvConfig.copperMaxHeight = builder.defineInRange("maxHeightCopper", 64, 1, 256);
		builder.pop();

		builder.push(CATEGORY_TIN);
		lvConfig.generateTin = builder.define("generateTin", true);
		lvConfig.tinClumpSize = builder.defineInRange("sizeTin", 6, 1, 64);
		lvConfig.tinPerChunk = builder.defineInRange("chanceTin", 10, 0, 256);
		lvConfig.tinMinHeight = builder.defineInRange("minHeightTin", 0, 0, 255);
		lvConfig.tinMaxHeight = builder.defineInRange("maxHeightTin", 64, 1, 256);
		builder.pop();

		builder.push(CATEGORY_DILITHIUM);
		lvConfig.generateDilithium = builder.define("generateDilithium", true);
		lvConfig.dilithiumClumpSize = builder.defineInRange("sizeDilithium", 16, 1, 64);
		lvConfig.dilithiumPerChunk = builder.defineInRange("chanceDilithium", 1, 0, 256);
		lvConfig.dilithiumPerChunkMoon = builder.comment("Dilithium veins per chunk on airless Advanced Rocketry planets.")
				.defineInRange("chanceDilithiumMoon", 10, 0, 256);
		lvConfig.dilithiumMinHeight = builder.defineInRange("minHeightDilithium", 0, 0, 255);
		lvConfig.dilithiumMaxHeight = builder.defineInRange("maxHeightDilithium", 64, 1, 256);
		builder.pop();

		builder.push(CATEGORY_ALUMINUM);
		lvConfig.generateAluminum = builder.define("generateAluminum", true);
		lvConfig.aluminumClumpSize = builder.defineInRange("sizeAluminum", 16, 1, 64);
		lvConfig.aluminumPerChunk = builder.defineInRange("chanceAluminum", 1, 0, 256);
		lvConfig.aluminumMinHeight = builder.defineInRange("minHeightAluminum", 0, 0, 255);
		lvConfig.aluminumMaxHeight = builder.defineInRange("maxHeightAluminum", 64, 1, 256);
		builder.pop();

		builder.push(CATEGORY_RUTILE);
		lvConfig.generateRutile = builder.define("generateRutile", true);
		lvConfig.rutileClumpSize = builder.defineInRange("sizeRutile", 6, 1, 64);
		lvConfig.rutilePerChunk = builder.defineInRange("chanceRutile", 6, 0, 256);
		lvConfig.rutileMinHeight = builder.defineInRange("minHeightRutile", 0, 0, 255);
		lvConfig.rutileMaxHeight = builder.defineInRange("maxHeightRutile", 64, 1, 256);
		builder.pop();

		builder.push(CATEGORY_IRIDIUM);
		lvConfig.generateIridium = builder.define("generateIridium", false);
		lvConfig.iridiumClumpSize = builder.defineInRange("sizeIridium", 16, 1, 64);
		lvConfig.iridiumPerChunk = builder.defineInRange("chanceIridium", 1, 0, 256);
		lvConfig.iridiumMinHeight = builder.defineInRange("minHeightIridium", 0, 0, 255);
		lvConfig.iridiumMaxHeight = builder.defineInRange("maxHeightIridium", 64, 1, 256);
		builder.pop();
		builder.pop();
	}

	public static LibVulpesConfig getCurrentConfig() {
		if(currentConfig == null) {
			logger.error("Had to generate a new config, this shouldn't happen");
			throw new NullPointerException("Expected config to not be null");
		}
		return currentConfig;
	}


	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Boolean> enableOreGen;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Boolean> generateCopper;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> copperPerChunk;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> copperClumpSize;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> copperMaxHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> copperMinHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> tinMaxHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> tinMinHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> rutileMaxHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> rutileMinHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> dilithiumMaxHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> dilithiumMinHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> aluminumMaxHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> aluminumMinHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Boolean> generateTin;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> tinPerChunk;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> tinClumpSize;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Boolean> generateDilithium;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> dilithiumClumpSize;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> dilithiumPerChunk;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> dilithiumPerChunkMoon;

	public ForgeConfigSpec.ConfigValue<Integer> aluminumPerChunk;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> aluminumClumpSize;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Boolean> generateAluminum;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Boolean> generateRutile;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> rutilePerChunk;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> rutileClumpSize;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Boolean> generateIridium;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> iridiumPerChunk;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> iridiumClumpSize;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> iridiumMaxHeight;

	@ConfigProperty
	public ForgeConfigSpec.ConfigValue<Integer> iridiumMinHeight;

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface ConfigProperty {
		boolean needsSync() default false;
		Class internalType() default Object.class;
		Class keyType() default Object.class;
		Class valueType() default Object.class;
	}
}
