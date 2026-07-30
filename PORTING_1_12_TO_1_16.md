# AdvancedRocketry 1.12.2 -> 1.16.5 porting status

This document tracks functional parity with the `1.12` Git branch. A class that
was renamed or replaced by a newer Minecraft/Forge implementation is not counted
as missing by itself; the player-visible behavior must be compared.

## Build baseline

- [x] Replace the unavailable Jenkins LibVulpes binary with the official
  `Advanced-Rocketry/LibVulpes` `1.16.5` source tree.
- [x] Remove the unscoped, unavailable Ivy repositories that caused dependency
  resolution timeouts for Forge, Minecraft and ASM artifacts.
- [x] Compile AdvancedRocketry and LibVulpes together with Java 8 by running
  `gradlew compileJava --no-daemon`.
- [x] Run the complete Gradle build.
- [x] Rebuild the exact audited source state with Java 8, including LibVulpes,
  resource processing, universal/deobf JAR creation and reobfuscation.
- [x] Use Forge `36.2.39`, the newest 1.16.5 build compatible with the existing
  ForgeGradle 4 toolchain while also containing the modern-Java ModLauncher fix.
- [x] Start a Forge client and reach the title screen with no ERROR/FATAL,
  missing-model, missing-texture, invalid-blockstate or failed-recipe log entry.
- [ ] Create/load a test world and exercise every restored feature.
- [ ] Start a dedicated Forge server and join it with a client.

## Confirmed preserved or replaced

- [x] Rockets and configurable rocket construction.
- [x] Planet/moon definitions and XML planet configuration.
- [x] Space stations, warp travel and space elevators.
- [x] Atmosphere handling, oxygen equipment and space suits.
- [x] Core multiblock machines and machine recipe infrastructure.
- [x] Asteroid mining and gas-collection missions.
- [x] Optical, composition, mass, microwave-power and ore-mapping satellites.
- [x] Crater, geode and volcano generation have 1.16 structure replacements.
- [x] Ore Dictionary use was replaced by the 1.16 tag system.
- [ ] Verify behavior, recipes, models and networking for every preserved entry
  in an actual client/server test; compilation alone is not functional proof.

## Confirmed missing

- [x] Restore and register the `density` satellite type
  (`SatelliteDensity`). This is also required to deserialize old satellite NBT
  whose `dataType` is `density`.
- [ ] Restore the biome-changing satellite end to end:
  `SatelliteBiomeChanger`, its controller item, biome-selection GUI/networking,
  satellite component/recipe, models, textures, translations and server-to-client
  chunk biome synchronization.
  - [x] Implement and register the satellite, component, controller, recipes,
    selection GUI/network packets, NBT migration and chunk-biome synchronization.
  - [ ] Verify scanning, selection, energy consumption, biome changes and save
    reload inside a real world.
- [ ] Restore the 1.12 `WorldCommand` functionality using the 1.16 Brigadier
  command API. `PlanetCommand` exists, but it does not replace every old
  world-management subcommand.
  - [x] Restore permission level 2 and aliases `/advrocketry` plus `/ar`.
  - [x] Restore `addTorch`, `addSolidBlockOverride`, `setGravity`, `fetch` and
    `beginTest`.
  - [x] Restore star get/set/generate operations.
  - [x] Fix broken planet-generation gravity arguments, planet get/set argument
    trees and `fillData` handling.
  - [x] Restore `reloadRecipes` through the 1.16 datapack reload path and port
    `dumpBiomes` to the 1.16 biome registry.
- [x] Restore the former Galacticraft oxygen override as an optional reflective
  bridge, fix lowercase 1.16 mod-ID detection, and restore `OverrideGCAir`.
  There is no official Galacticraft 1.16.5 API artifact to compile or test
  against, so the bridge deliberately has no hard dependency.
- [x] Replace the former sawmill JEI category with the 1.16 Cutting Machine
  category and restore the configurable six-planks-from-one-vanilla-log recipes.
- [x] Restore the invisible orbital-laser shaft light block and fix the
  MiningDrill chunk-ticket release (including the null dereference on shutdown).
- [x] Register deployed/gas-collection rockets as their own entity type instead
  of serializing and spawning them as normal rockets; restore old entity-ID
  aliases for dummy, UI, abducted-item, elevator and hovercraft entities.
- [ ] Restore the remaining standalone 1.12 items and their behavior.
  - [x] Re-register the Space Elevator Chip and migrate its old numeric
    dimension-position NBT to namespaced 1.16 dimension IDs.
  - [x] Re-register Thermite with its 6000-tick fuel value and restore the
    Thermite/Thermite Torch recipe chain.
  - [x] Re-register and repair the Basic Laser Gun use duration, ray tracing,
    damage, block breaking, sound and laser packets.
  - [x] Restore all four 1.12 spacesuit tank tiers and their original
    1000/2000/4000/8000 mB capacities (including the omitted iron tier).
  - [x] Restore the Earthbright Visor together with the missing
    distance-dependent planetary light behavior it used to override.
  - [ ] Runtime-test the restored standalone items in survival and multiplayer.

`SatelliteDefunct` also disappeared, but the 1.12 registry never instantiated
it: unknown satellite types already returned `null`, and the class had no
references. It is therefore not counted as a lost player-facing feature unless
old-save testing proves otherwise.

## Confirmed partial

- [ ] Complete terraforming. The 1.16 atmosphere terraformer currently changes
  atmosphere density and now again drives the biome transition; runtime testing
  is still required.
- [x] Restore the biome-changing portion of the atmosphere terraformer, including
  its biome-changer controller slot and validation.
- [x] Re-enable the `DimensionProperties#setAtmosphereDensity` transition that
  updates and persists terraformed biomes.
- [x] Replace the removed 1.12 chunk-provider cache reset with a safe 1.16 biome
  update strategy for loaded and newly generated chunks.
- [x] Restore the 1.12 controls for enabling terraforming, optional fluid
  consumption, biome-update speed and non-AR-world terraforming.
- [x] Repair satellite-chip NBT writes and ResourceLocation handling so the
  restored biome controller remains linked after save/reload.
- [ ] Verify the atmosphere threshold, controller removal, fluid consumption,
  gradual loaded-chunk conversion and new-chunk biomes in a real world.

## Compatibility and content audit still required

- [x] Compare every 1.12 registry ID with the 1.16 item/block/entity/tile
  registries and add missing mappings for renamed IDs.
  - [x] Add safe one-to-one block/item aliases for renamed machines, rocket
    parts, station controls, plants and fluid blocks.
  - [x] Add renamed entity aliases and retain the old deployed-rocket save ID.
  - [x] Add old 1.12 TileEntity aliases for every machine that branch actually
    registered, so remapped blocks retain their machine NBT/type on load.
  - [x] Migrate old Space Elevator Chip dimension-position NBT.
  - [x] Reserve and convert old metadata-container items (`itemcircuitplate`, `ic`,
    `misc`, `itemupgrade`, `pressuretank`, `satellitepowersource` and
    `satelliteprimaryfunction`) without deliberately collapsing all variants
    into one item. Conversion covers player, container, dropped-item, chunk
    inventory, rocket inventory and nested spacesuit-module stacks.
  - [x] Resolve registry-name collisions where a 1.16 item reused a 1.12 ID
    with different meaning (`biomechanger`, `pressuretank`, `sawblade` and
    `solarpanel`).
  - [x] Separate the collapsed `lens` item and `blocklens` block IDs again,
    restore the non-consumed laser-etcher lens input, restore both circuit-wafer
    etching recipes to the correct machine, and make the orbital drill accept
    the original item lens.
  - [x] Register the omitted `precisionlaseretcher` recipe serializer; without
    it, Minecraft rejected every recipe for that otherwise registered machine.
  - [x] Preserve both original circuit-wafer production routes: the
    single-output Precision Assembler recipes and the two-output,
    lens-preserving Precision Laser Etcher recipes.
  - [x] Preserve the old metadata-based `loader` block with all seven variants
    and bind each variant to its matching current TileEntity type.
  - [x] Preserve LibVulpes' metadata-based `product*`, `ore0`, `metal0`, `coil0`,
    `hatch` and `battery` stacks. Legacy carriers keep the original material or
    hatch variant until inventory migration replaces them with the split 1.16
    IDs; old fluid hatches also retain their saved `outputOnly` state.
  - [x] Add LibVulpes aliases for renamed structure blocks, power plugs,
    generator, motors and TileEntity IDs, and accept legacy hatch/coil states in
    multiblocks with their original coil speed multipliers.
  - [x] Add `scripts/audit_1_12_item_block_registry.ps1` and prove coverage for
    all 101 actually registered 1.12 block IDs, 35 explicit item IDs and 94
    automatically generated BlockItem IDs through current IDs, one-to-one
    aliases or metadata-aware migrations. Restore the omitted hidden
    `forcefield` and `lightsource` BlockItems and remap the old
    `airlock_door` BlockItem to the usable `smallairlockdoor` item.
  - [x] Add `scripts/audit_1_12_entity_tile_registry.ps1` and prove coverage for
    all ten 1.12 entity IDs and all 54 normally reachable TileEntity IDs. The
    remaining three registered TileEntity IDs belonged to data, energy and
    liquid pipe blocks whose block registrations were already commented out in
    1.12; the audit verifies that dead-code classification explicitly.
- [ ] Test loading copied 1.12 NBT fixtures for satellites, dimensions, stations,
  rockets, missions and identification chips.
  - [x] Add `scripts/audit_1_12_nbt_compat.ps1` and retain readers for the
    numeric 1.12 dimension, star, station and biome IDs. The migration covers
    satellites, planet hierarchy/biomes, rocket payloads, missions,
    `SpacePosition`, guidance computers, space elevators and all three
    identification-chip types.
  - [x] Accept the deployed 1.12 spellings `satallites`, `satallite` and
    rocket-payload `DataType`, while continuing to write the corrected 1.16
    spellings.
  - [ ] Load binary NBT fixtures in Minecraft and verify their converted values
    after a second save/reload. The source audit proves reader coverage but is
    not a substitute for Forge registry remapping in a real upgraded world.
  - [ ] Confirm the metadata fixtures before Minecraft's vanilla 1.13
    flattening pass. That pass removes the root `Damage` field from unknown
    mod items, so a direct 1.12-to-1.16 world upgrade may require an offline
    pre-conversion step to remain variant-perfect.
- [x] Audit configuration options that disappeared or moved to LibVulpes,
  including ore generation, laser drill, gravity controller, sawmill,
  Galacticraft air, terraforming and cross-mod material recipes.
  - [x] Restore the missing `makeMaterialsForOtherMods` switch and rebuild the
    tag-driven rolling-machine/lathe recipes for external ingots, plates and rods.
  - [x] Restore LibVulpes tin generation/material resources and the missing tin
    rolling/crusher recipes.
  - [x] Classify the remaining old fields: ore settings moved to LibVulpes;
    `enableTerraforming` became `allowTerraforming`; RF/EU conversion settings
    became obsolete with Forge Energy; rocket fuel tanks now store real fluid mB;
    `skyOverride` was merged into `planetSkyOverride`; and numeric `spaceDimId`
    became obsolete with namespaced dimensions.
- [x] Classify and restore removed world generators/providers by behavior.
  - [x] Restore custom planet caves and canyons, two-layer cave planets,
    two-band asteroid terrain, vanilla structures/villages, XML and
    temperature/pressure ore generation, and the airless-world dilithium boost.
  - [x] Retain the 1.16 structure replacements for craters, geodes and volcanoes.
- [ ] Compare all recipes, loot tables, tags, advancements, models, textures,
  translations, sounds and JEI categories.
  - [x] Restore and audit seven modern non-English locale JSON files. Every
    translated value in `de_de`, `es_es`, `fi_fi`, `fr_fr`, `ru_ru`, `uk_ua`
    and `zh_cn` is traced to its corresponding 1.12 language file by
    `scripts/audit_1_12_translations.ps1`.
  - [x] Compare all 16 sound-event IDs and all 17 OGG assets with 1.12; no
    sound content is missing.
  - [x] Compare all 17 advancements with 1.12. The resource-name difference is
    the already repaired `flightofpheonix` -> `flightofthephoenix` typo.
  - [x] Audit block loot against the current block and item registries. Rename
    the mistyped Airlock Door, Space Elevator and Space Station Assembler loot
    paths and restore the missing Loader, Laser, Nuclear Core, Nuclear Rocket
    Engine, Nuclear Working Fluid Tank, Oxidizer Fuel Tank, Precision Laser
    Etcher and Solar Array drops. `scripts/audit_1_16_block_loot.ps1` verifies
    all 112 registered blocks plus both generated material blocks and both
    intentional no-drop blocks.
- [x] Fix the invalid station-light ingredient and verify that the client recipe
  loader reports no parsing error.
- [x] Restore the omitted Space Elevator Chip precision-assembler recipe and
  preserve the old bone-to-eight-bone-meal chemical-reactor result.
- [x] Restore the original precision-assembler costs for the biome satellite
  component/controller and the original colorless-glass-pane lens recipe.
- [x] Restore the separate 1.12 `solarpanel` block, its original recipe and
  registry identity. The Microwave Receiver again uses that block, the Solar
  Generator consumes it, and eight panels plus a Structure Tower again produce
  eight `solararraypanel` blocks; the satellite component remains the separate
  `basicsolarpanel` item.
- [x] Confirm that `pipesealer.json` and `pipesealer_alt.json` differed only by
  the synonymous 1.12 Ore Dictionary names `stickIron`/`rodIron`; the single
  1.16 `forge:rods/iron` recipe preserves both inputs without a duplicate recipe.
- [x] Remap the old LibVulpes `holoprojector` item ID to the 1.16
  `holo_projector` item.
- [x] Restore the Holo Projector's selected-machine tooltip and material list.
  The 1.16 port tested an integer machine ID against a list of machine objects,
  so the 1.12 information was never displayed. Invalid or incomplete projector
  NBT and network data are now bounds-checked as well.
- [x] Repair the Flight of the Phoenix advancement lookup, whose direct 1.16
  resource path still used the old custom-trigger misspelling.
- [x] Remove legacy 1.12 `inventory` blockstate variants, port rocket-fire
  states to `age`, restore missing AdvancedRocketry/LibVulpes models, and verify
  a clean client resource load.
- [x] Prevent old-item inventory migration from opening pending loot tables during
  `ChunkEvent.Load`, which deadlocked integrated-server world generation.
- [x] Re-run Java compilation, parse every AdvancedRocketry/LibVulpes resource
  JSON file, and reach the client sound/model-complete state after the solar and
  LibVulpes compatibility restoration with zero ERROR/FATAL, missing-model,
  missing-texture, blockstate, recipe or duplicate-registration log entries.
- [x] Add reproducible read-only recipe audits and compare every 1.12 crafting
  and machine recipe after normalizing metadata containers, renamed registry
  IDs, Ore Dictionary names, tags and fluid IDs (119 crafting recipes plus 37
  machine recipes; `_factories.json` is serializer metadata, not a recipe).
  - [x] Restore all 1.12 crafting shapes, ingredients and result counts,
    including the original circuit positions/materials, tank wildcard,
    Biome Changer component, batteries, glass/lens requirements, Solar Array,
    spacesuit and rocket-part recipes.
  - [x] Restore all 1.12 machine-recipe ingredients, time, energy and outputs,
    including tin in the Atmosphere Analyzer, three Titanium Aluminide ingots
    and one Advanced Circuit in the Nuclear Core recipe.
  - [x] `scripts/audit_1_12_machine_recipes.ps1` reports zero missing and zero
    changed recipes (and runs the crafting audit first).
- [x] Mark LibVulpes machine recipes as dynamic recipes. An integrated-client
  smoke test synchronized all 84 explicit/generated machine recipes without a
  parse failure, but vanilla tried to categorize them in its crafting recipe
  book and logged one warning per recipe. `IRecipe#isDynamic()` now correctly
  keeps these machine/JEI-only recipes out of the vanilla recipe book.
- [ ] Audit TODO/FIXME and placeholder returns only where they lie on a 1.12
  feature path; raw counts include legitimate interface and optional behavior.

## Runtime verification gates

- [x] Reach the client title/sound/model-complete state without an ERROR/FATAL,
  missing model/texture, invalid blockstate or recipe parse failure.
- [x] Generate and join an isolated integrated-server world, synchronize the
  modded recipe set, create the `advancedrocketry:space` dimension and shut the
  world down with all chunks saved and no ERROR/FATAL entry.
- [ ] Repeat the integrated-world recipe synchronization after the
  `LibVulpesRecipe#isDynamic()` correction and confirm that the previous 84
  vanilla recipe-book category warnings are gone. Do not confirm Minecraft's
  Experimental Settings warning without explicit user approval.
- [ ] Run `/ar beginTest`, then manually exercise the restored satellites,
  terraforming, rockets, machines, Holo Projector, standalone items, save/reload
  and 1.12 NBT fixtures.
- [ ] Start a dedicated server only after the user has personally accepted its
  EULA; automation must not change `eula.txt`. Join it with a matching client
  and repeat the networking/save tests.

## Completion rule

The 1.16.5 port is complete only when all confirmed missing/partial items are
implemented, the compatibility/content audit has no unexplained losses, the full
build succeeds, and both client plus dedicated-server smoke tests pass.
