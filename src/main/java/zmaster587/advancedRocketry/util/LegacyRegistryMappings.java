package zmaster587.advancedRocketry.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.api.AdvancedRocketryItems;
import zmaster587.advancedRocketry.api.Constants;

/**
 * One-to-one registry aliases for worlds and inventories created by the 1.12
 * release. Metadata containers are deliberately not handled here: remapping an
 * entire old ID to one former variant would silently corrupt stacks.
 */
public final class LegacyRegistryMappings {

	private LegacyRegistryMappings() {
	}

	public static void remapBlocks(RegistryEvent.MissingMappings<Block> event) {
		for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
			if (!Constants.modId.equals(mapping.key.getNamespace()))
				continue;

			Block target = blockTarget(mapping.key.getPath());
			if (target != null)
				mapping.remap(target);
		}
	}

	public static void remapItems(RegistryEvent.MissingMappings<Item> event) {
		for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
			if (!Constants.modId.equals(mapping.key.getNamespace()))
				continue;

			Item target = itemTarget(mapping.key.getPath());
			if (target != null)
				mapping.remap(target);
		}
	}

	public static void remapEntities(RegistryEvent.MissingMappings<EntityType<?>> event) {
		for (RegistryEvent.MissingMappings.Mapping<EntityType<?>> mapping : event.getAllMappings()) {
			if (!Constants.modId.equals(mapping.key.getNamespace()))
				continue;

			EntityType<?> target;
			switch (mapping.key.getPath()) {
				case "mountdummy": target = zmaster587.advancedRocketry.api.AdvancedRocketryEntities.ENTITY_DUMMY; break;
				case "arabducteditem": target = zmaster587.advancedRocketry.api.AdvancedRocketryEntities.ENTITY_ITEM_ABDUCTED; break;
				case "arplanetuiitem": target = zmaster587.advancedRocketry.api.AdvancedRocketryEntities.ENTITY_UIPLANET; break;
				case "arplanetuibutton": target = zmaster587.advancedRocketry.api.AdvancedRocketryEntities.ENTITY_UIBUTTON; break;
				case "arstaruibutton": target = zmaster587.advancedRocketry.api.AdvancedRocketryEntities.ENTITY_UISTAR; break;
				case "arspaceelevatorcapsule": target = zmaster587.advancedRocketry.api.AdvancedRocketryEntities.ENTITY_ELEVATOR_CAPSULE; break;
				case "arhovercraft": target = zmaster587.advancedRocketry.api.AdvancedRocketryEntities.ENTITY_HOVER_CRAFT; break;
				default: target = null;
			}
			if (target != null)
				mapping.remap(target);
		}
	}

	public static void remapTileEntities(RegistryEvent.MissingMappings<TileEntityType<?>> event) {
		for (RegistryEvent.MissingMappings.Mapping<TileEntityType<?>> mapping : event.getAllMappings()) {
			if (!Constants.modId.equals(mapping.key.getNamespace()))
				continue;

			TileEntityType<?> target = tileTarget(mapping.key.getPath());
			if (target != null)
				mapping.remap(target);
		}
	}

	private static TileEntityType<?> tileTarget(String oldPath) {
		switch (oldPath) {
			case "arrocketbuilder": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ROCKET_ASSEMBLER;
			case "arwarpcore": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_WARP_CORE;
			case "arfuelingstation": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_FUELING_STATION;
			case "armonitoringstation": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ROCKET_CONTROL_CENTER;
			case "arspacelaser": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ORBITAL_LASER_DRILL;
			case "arprecisionassembler": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_PRECISION_ASSEMBLER;
			case "arobservatory": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_OBSERVATORY;
			case "arcrystallizer": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_CRYSTALLIZER;
			case "arcuttingmachine": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_CUTTING_MACHINE;
			case "ardatabus": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_DATA_BUS;
			case "arsatellitehatch": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_SATELLITE_BAY;
			case "arguidancecomputerhatch": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_GUIDANCE_COMPUTER__ACCESS_HATCH;
			case "arsatellitebuilder": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_SATELLITE_ASSEMBLER;
			case "artileentitysatellitecontrolcenter": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_SATELLITE_CONTROL_CENTER;
			case "arplanetanalyser": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ASTROBODY_DATA_PROCESSOR;
			case "arguidancecomputer": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_GUIDANCE_COMPUTER;
			case "arelectricarcfurnace": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ARC_FURNACE;
			case "artileplanetselector": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_PLANET_SELECTOR;
			case "artilematerial": return zmaster587.libVulpes.api.LibVulpesTileEntityTypes.TILE_PLACEHOLDER;
			case "artilelathe": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_LATHE;
			case "artilemetalbender": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ROLLING;
			case "arstationbuilder": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_STATION_BUILDER;
			case "arelectrolyser": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ELECTROLYZER;
			case "archemicalreactor": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_CHEMICAL_REACTOR;
			case "aroxygenvent": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_OXYGEN_VENT;
			case "aroxygencharger": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_GAS_CHARGE_PAD;
			case "arco2scrubber": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_CO2_SCRUBBER;
			case "arstationmonitor": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_WARP_CONTROLLER;
			case "aroxygendetector": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ATMOSPHERE_DETECTOR;
			case "arorientationcontrol": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ORIENTATION_CONTROLLER;
			case "argravitycontrol": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_STATION_GRAVITY_CONTROLLER;
			case "armicrowavereciever": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_MICROWAVE_RECEIVER;
			case "arsuitworkstation": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_WORK_STATION;
			case "arrocketloader": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ROCKET_LOADER;
			case "arrocketunloader": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ROCKET_UNLOADER;
			case "arbiomescanner": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_BIOME_SCANNER;
			case "arattterraformer": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_TERRAFORMER;
			case "arlandingpad": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_LANDING_PAD;
			case "arstationdeployablerocketassembler": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_STATION_DEPLOYED_ASSEMBLER;
			case "arfluidtank": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_FLUID_TANK;
			case "arfluidunloader": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_FLUID_UNLOADER;
			case "arfluidloader": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_FLUID_LOADER;
			case "arsolargenerator": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_SOLAR_PANEL;
			case "ardockingport": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_DOCKING_PORT;
			case "arstationaltitudecontroller": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_ALT_CONTROLLER;
			case "arrailgun": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_RAILGUN;
			case "arplanetholoselector": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_HOLOGRAM;
			case "arforcefieldprojector": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_FORCE_FIELD_PROJECTOR;
			case "arblockseal": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_SEAL;
			case "arspaceelevator": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_SPACE_ELEVATOR;
			case "arbeacon": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_BEACON;
			case "artransciever": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_WIRELESS_TRANSCEIVER;
			case "arblackholegenerator": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_BLACK_HOLE_GENERATOR;
			case "arpump": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_PUMP;
			case "arcentrifuge": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_CENTRIFUGE;
			case "arprecisionlaseretcher": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_PREC_LASER_ETCHER;
			case "arsolararray": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_SOLAR_ARRAY;
			case "argravitymachine": return zmaster587.advancedRocketry.api.AdvancedRocketryTileEntityType.TILE_AREA_GRAVITY_CONTROLLER;
			default: return null;
		}
	}

	private static Block blockTarget(String oldPath) {
		switch (oldPath) {
			case "lens":
			case "blocklens": return AdvancedRocketryBlocks.blockLens;
			case "solarpanel": return AdvancedRocketryBlocks.blockSolarPanel;
			case "sawblade": return AdvancedRocketryBlocks.blockSawBlade;
			case "platepress": return AdvancedRocketryBlocks.blockSmallPlatePress;
			case "vacuumlaser": return AdvancedRocketryBlocks.blockLaser;
			case "blockpump": return AdvancedRocketryBlocks.blockPump;
			case "liquidtank": return AdvancedRocketryBlocks.blockPressureTank;
			case "wirelesstransciever": return AdvancedRocketryBlocks.blockWirelessTransceiver;
			case "precisionassemblingmachine": return AdvancedRocketryBlocks.blockPrecisionAssembler;
			case "electrolyser": return AdvancedRocketryBlocks.blockElectrolyzer;
			case "planetanalyser": return AdvancedRocketryBlocks.blockAstrobodyDataProcessor;
			case "satellitebuilder": return AdvancedRocketryBlocks.blockSatelliteAssembler;
			case "microwavereciever": return AdvancedRocketryBlocks.blockMicrowaveReceiver;
			case "spaceelevatorcontroller": return AdvancedRocketryBlocks.blockSpaceElevator;
			case "gravitymachine": return AdvancedRocketryBlocks.blockAreaGravityController;
			case "spacelaser": return AdvancedRocketryBlocks.blockOrbitalLaserDrill;
			case "stationmarker": return AdvancedRocketryBlocks.blockStationDockingPort;
			case "rocketmotor": return AdvancedRocketryBlocks.blockMonopropellantEngine;
			case "bipropellantrocketmotor": return AdvancedRocketryBlocks.blockBipropellantEngine;
			case "advrocketmotor": return AdvancedRocketryBlocks.blockAdvancedMonopropellantEngine;
			case "advbipropellantrocketmotor": return AdvancedRocketryBlocks.blockAdvancedBipropellantEngine;
			case "nuclearrocketmotor": return AdvancedRocketryBlocks.blockNuclearEngine;
			case "fueltank": return AdvancedRocketryBlocks.blockMonopropellantFuelTank;
			case "nuclearfueltank": return AdvancedRocketryBlocks.blockNuclearWorkingFluidTank;
			case "rocketbuilder": return AdvancedRocketryBlocks.blockRocketAssembler;
			case "stationbuilder": return AdvancedRocketryBlocks.blockSpaceStationAssembler;
			case "deployablerocketbuilder": return AdvancedRocketryBlocks.blockUnmannedRocketAssembler;
			case "monitoringstation": return AdvancedRocketryBlocks.blockRocketControlCenter;
			case "warpmonitor": return AdvancedRocketryBlocks.blockWarpController;
			case "planetholoselector": return AdvancedRocketryBlocks.blockHolographicPlanetSelector;
			case "oxygencharger": return AdvancedRocketryBlocks.blockGasChargePad;
			case "oxygendetection": return AdvancedRocketryBlocks.blockAtmosphereDetector;
			case "pipesealer": return AdvancedRocketryBlocks.blockSeal;
			case "circlelight": return AdvancedRocketryBlocks.blockStationLight;
			case "hotturf": return AdvancedRocketryBlocks.blockOxidizedFerricSand;
			case "alienwood": return AdvancedRocketryBlocks.blockLightwoodLog;
			case "alienleaves": return AdvancedRocketryBlocks.blockLightwoodLeaves;
			case "aliensapling": return AdvancedRocketryBlocks.blockLightwoodSapling;
			case "planks": return AdvancedRocketryBlocks.blockLightwoodPlanks;
			case "oxygenfluid": return AdvancedRocketryBlocks.blockOxygenFluid.get();
			case "hydrogenfluid": return AdvancedRocketryBlocks.blockHydrogenFluid.get();
			case "nitrogenfluid": return AdvancedRocketryBlocks.blockNitrogenFluid.get();
			case "rocketfuel": return AdvancedRocketryBlocks.blockFuelFluid.get();
			case "enrichedlavafluid": return AdvancedRocketryBlocks.blockEnrichedLavaFluid.get();
			default: return null;
		}
	}

	private static Item itemTarget(String oldPath) {
		switch (oldPath) {
			case "wafer": return AdvancedRocketryItems.itemSiliconWafer;
			case "satelliteidchip": return AdvancedRocketryItems.itemSatelliteChip;
			case "planetidchip": return AdvancedRocketryItems.itemPlanetChip;
			case "spacestation": return AdvancedRocketryItems.itemSpaceStationContainer;
			case "sawbladeiron": return AdvancedRocketryItems.itemSawBlade;
			case "iquartzcrucible": return AdvancedRocketryItems.itemQuartzCrucible;
			case "atmanalyser": return AdvancedRocketryItems.itemAtmosphereAnalyzer;
			case "beaconfinder": return AdvancedRocketryItems.itemBeaconFinderUpgrade;
			case "spacehelmet": return AdvancedRocketryItems.itemSpaceSuitHelmet;
			case "spacechestplate": return AdvancedRocketryItems.itemSpaceSuitChestpiece;
			case "spaceleggings": return AdvancedRocketryItems.itemSpaceSuitLeggings;
			case "spaceboots": return AdvancedRocketryItems.itemSpaceSuitBoots;
			case "basiclasergun": return AdvancedRocketryItems.itemBasicLaserGun;
			case "biomechangerremote": return AdvancedRocketryItems.itemBiomeChangerRemote;
			case "sawbladeassembly": return AdvancedRocketryItems.itemSawbladeAssembly;
			case "solarpanel": return AdvancedRocketryItems.itemSolarPanelBlock;
			case "solararraypanel": return AdvancedRocketryItems.itemSolarArrayPanel;
			case "airlock_door": return AdvancedRocketryItems.itemSmallAirlockDoor;

			case "blocklens": return AdvancedRocketryItems.itemLensBlock;
			case "platepress": return AdvancedRocketryItems.itemSmallPlatePress;
			case "vacuumlaser": return AdvancedRocketryItems.itemLaser;
			case "blockpump": return AdvancedRocketryItems.itemPump;
			case "liquidtank": return AdvancedRocketryItems.itemPressureTank;
			case "wirelesstransciever": return AdvancedRocketryItems.itemWirelessTransceiver;
			case "precisionassemblingmachine": return AdvancedRocketryItems.itemPrecisionAssembler;
			case "electrolyser": return AdvancedRocketryItems.itemElectrolyzer;
			case "planetanalyser": return AdvancedRocketryItems.itemAstrobodyDataProcessor;
			case "satellitebuilder": return AdvancedRocketryItems.itemSatelliteAssembler;
			case "microwavereciever": return AdvancedRocketryItems.itemMicrowaveReceiver;
			case "spaceelevatorcontroller": return AdvancedRocketryItems.itemSpaceElevator;
			case "gravitymachine": return AdvancedRocketryItems.itemAreaGravityController;
			case "spacelaser": return AdvancedRocketryItems.itemOrbitalLaserDrill;
			case "stationmarker": return AdvancedRocketryItems.itemStationDockingPort;
			case "rocketmotor": return AdvancedRocketryItems.itemMonopropellantEngine;
			case "bipropellantrocketmotor": return AdvancedRocketryItems.itemBipropellantEngine;
			case "advrocketmotor": return AdvancedRocketryItems.itemAdvancedMonopropellantEngine;
			case "advbipropellantrocketmotor": return AdvancedRocketryItems.itemAdvancedBipropellantEngine;
			case "nuclearrocketmotor": return AdvancedRocketryItems.itemNuclearEngine;
			case "fueltank": return AdvancedRocketryItems.itemMonopropellantFuelTank;
			case "nuclearfueltank": return AdvancedRocketryItems.itemNuclearWorkingFluidTank;
			case "rocketbuilder": return AdvancedRocketryItems.itemRocketAssembler;
			case "stationbuilder": return AdvancedRocketryItems.itemSpaceStationAssembler;
			case "deployablerocketbuilder": return AdvancedRocketryItems.itemUnmannedRocketAssembler;
			case "monitoringstation": return AdvancedRocketryItems.itemRocketControlCenter;
			case "warpmonitor": return AdvancedRocketryItems.itemWarpController;
			case "planetholoselector": return AdvancedRocketryItems.itemHolographicPlanetSelector;
			case "oxygencharger": return AdvancedRocketryItems.itemGasChargePad;
			case "oxygendetection": return AdvancedRocketryItems.itemAtmosphereDetector;
			case "pipesealer": return AdvancedRocketryItems.itemSeal;
			case "circlelight": return AdvancedRocketryItems.itemStationLight;
			case "hotturf": return AdvancedRocketryItems.itemOxidizedFerricSand;
			case "alienwood": return AdvancedRocketryItems.itemLightwoodLog;
			case "alienleaves": return AdvancedRocketryItems.itemLightwoodLeaves;
			case "aliensapling": return AdvancedRocketryItems.itemLightwoodSapling;
			case "planks": return AdvancedRocketryItems.itemLightwoodPlanks;
			default: return null;
		}
	}
}
