package zmaster587.advancedRocketry.world.decoration;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.ScatteredStructurePiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.fluids.IFluidBlock;
import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.advancedRocketry.world.decoration.StructureCrater.CraterType;
import zmaster587.libVulpes.util.ZUtils;

public class StructurePieceCrater extends ScatteredStructurePiece {

	int radius;
	int xCenter, zCenter;
	CraterType craterType;

	public StructurePieceCrater(Random random, int x, int z) {
		this(random, x, z, CraterType.NORMAL, false);
	}

	public StructurePieceCrater(Random random, int x, int z, CraterType craterType, boolean airless) {
		this(random, x, z, craterType, chooseRadius(random, craterType, airless));
	}

	private StructurePieceCrater(Random random, int x, int z, CraterType craterType, int radius) {
		super(AdvancedRocketryBiomes.STRUCTURE_PIECE_CRATER, random, x, 64, z,
				getStructureDiameter(craterType), 64, getStructureDiameter(craterType));
		this.setCoordBaseMode(null);
		this.radius = radius;
		xCenter = x;
		zCenter = z;
		this.craterType = craterType;
	}

    public StructurePieceCrater(TemplateManager mgr, CompoundNBT piece) {
        super(AdvancedRocketryBiomes.STRUCTURE_PIECE_CRATER, piece);
        radius = piece.getInt("Radius");
        xCenter = piece.getInt("xCenter");
        zCenter =  piece.getInt("zCenter");
		if(piece.contains("CraterType")) {
			int savedType = piece.getInt("CraterType");
			craterType = savedType >= 0 && savedType < CraterType.values().length
					? CraterType.values()[savedType]
					: CraterType.NORMAL;
		}
		else {
			craterType = CraterType.NORMAL;
		}
     }
	

    /**
     * (abstract) Helper method to read subclass data from NBT
     */
    protected void readAdditional(CompoundNBT tagCompound) {
       super.readAdditional(tagCompound);
       tagCompound.putInt("Radius", radius);
       tagCompound.putInt("xCenter", xCenter);
       tagCompound.putInt("zCenter", zCenter);
       tagCompound.putInt("CraterType", craterType.ordinal());
    }

	public boolean func_230383_a_(ISeedReader world, StructureManager structureMgr, ChunkGenerator chunkGen, Random rand, MutableBoundingBox bb, ChunkPos chunkPos, BlockPos blockPos)
	{

		DimensionProperties props = DimensionManager.getInstance().getDimensionProperties(ZUtils.getDimensionIdentifier(world.getWorld()));

		List<BlockState> ores = props.craterOres.stream()
				.map(s-> Block.getBlockFromItem(s.getItem()).getDefaultState())
				.filter(state -> state.getBlock() != Blocks.AIR)
				.collect(Collectors.toList());

		// Shape parameters must be identical in every chunk touched by the
		// structure. The Random passed to this method is chunk-local.
		Random shapeRandom = new Random(getShapeSeed());
		int coefficientBound = craterType == CraterType.SMALL ? 15 : 10;
		int[] sinCoefficients = {
				shapeRandom.nextInt(coefficientBound) + 1,
				shapeRandom.nextInt(coefficientBound) + 1,
				shapeRandom.nextInt(coefficientBound) + 1,
				shapeRandom.nextInt(coefficientBound) + 1,
				shapeRandom.nextInt(coefficientBound) + 1
		};
		int baseRadius = radius;
		boolean large = baseRadius > 32;
		int numBulges = craterType == CraterType.SMALL ? 3 : shapeRandom.nextInt(large ? 5 : 4) + 1;
		boolean centerSpire = craterType == CraterType.HUGE && shapeRandom.nextInt(4) == 0;

		//Turn the coordinates from chunk stuff into their actual values
		int xCoord = chunkPos.x << 4;
		int zCoord = chunkPos.z << 4;
		int centerX = (this.boundingBox.minX + this.boundingBox.maxX) / 2;
		int centerZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2;

		//Set up fluid fill, if needed
		BlockState fillBlock = Blocks.AIR.getDefaultState();
		int fluidMaxY = 0;
		for(int relx = 15; relx >= 0; relx--) {
			for (int relz = 15; relz >= 0; relz--) {
				for (int y = 254; y >= 0; y--) {

					int x = relx + xCoord;
					int z = relz + zCoord;

					if (getBlockStateFromPos(world.getWorld(), x, y, z, bb).getBlock() instanceof FlowingFluidBlock || getBlockStateFromPos(world.getWorld(), x, y, z, bb).getBlock() instanceof IFluidBlock) {
						if (y > fluidMaxY) {
							fillBlock = getBlockStateFromPos(world.getWorld(), x, y, z, bb);
							fluidMaxY = y;
						}
					} else if (getBlockStateFromPos(world.getWorld(), x, y, z, bb).getBlock() != Blocks.AIR)
						break;
				}
			}
		}

		//Actually generate the crater
		for(int relx = 15; relx >= 0; relx--) {
			for(int relz = 15; relz >= 0; relz--) {
				for (int y = 254; y >= 0; y--) {

					int x = relx + xCoord;
					int z = relz + zCoord;
					int relativeX = x - centerX;
					int relativeZ = z - centerZ;

					if (y <= fluidMaxY && fillBlock.getBlock() != Blocks.AIR && getBlockStateFromPos(world.getWorld(), x, y, z, bb).getBlock() == Blocks.AIR) {
						this.setBlockState(world, fillBlock, x, y, z, bb);
					}
					if (!isCraterIgnoredBlock(getBlockStateFromPos(world.getWorld(), x, y, z, bb).getBlock())) {
						//Get us some funky radii up in here
						int radius = getRadius(baseRadius, relativeX, relativeZ, numBulges, sinCoefficients);

						//Standard inverseHalfRadius stuff
						int distancesSquared = relativeX * relativeX + relativeZ * relativeZ;
						int blockRadius = (int)Math.sqrt(distancesSquared);
						double depthDivisor = craterType == CraterType.HUGE
								? baseRadius * (baseRadius > 256 ? 4D : baseRadius > 128 ? 3D : 2.25D)
								: radius * 2D;
						int inversePartialSquareRadius = (int)((radius * radius - distancesSquared) / depthDivisor);
						int inverseRadius = radius - blockRadius;

						//Places filler blocks to excavate the crater
						for (int dist = 0; dist < inversePartialSquareRadius; dist++) {
							if (y - dist > 2) {
								int maximumDepth = craterType == CraterType.HUGE ? 27 : large ? 16 : 12;
								int targetY = y - Math.min(maximumDepth, dist);
								this.setBlockState(world, (targetY <= fluidMaxY) ? fillBlock : Blocks.AIR.getDefaultState(), x, targetY, z, bb);
							}
						}

						//Places blocks to form the ridges
						double ridgeSize = Math.max(1, (12 * (radius)/64.0));
						double outerRidgeLimit = craterType == CraterType.HUGE ? -3D * radius
								: craterType == CraterType.SMALL ? -1.125D * radius
								: -2D * radius;
						if (inverseRadius <= radius/4 && inverseRadius > outerRidgeLimit) {
							//The graph of this function and the old one can be found here https://www.desmos.com/calculator/x02rgy2wlf
							for (int dist = -1; dist < 9 * ridgeSize * ((1 - inverseRadius)/(0.8 * radius + (inverseRadius - 1) * (inverseRadius - 1))) - 1.06; dist++) {
								//Place the bank thrown up by the impact, and have some of the farthest be dispersed
								if (y + dist < 255 && inverseRadius > -0.5 * radius)
									this.setBlockState(world, this.getBlockToPlace(world.getWorld(), x, z, ores, rand), x, y + dist, z, bb);
								else if (y + dist < 255 && inverseRadius >= -0.625 * radius)
									this.setBlockState(world, this.getBlockToPlace(world.getWorld(), x, z, ores, rand), x, y + dist, z, bb);
								else if (y + dist < 255 && inverseRadius < -0.625 * radius  && rand.nextInt(Math.abs(inverseRadius + (int)(radius * 0.625)) + 1) == 0)
									this.setBlockState(world, this.getBlockToPlace(world.getWorld(), x, z, ores, rand), x, y + dist, z, bb);

								//Ejecta blocks on top, then ejecta blocks below farther out
								if (rand.nextInt(Math.abs(inverseRadius) + 1) == 0 && baseRadius > 40) {
									double ejectaRadius = -(1.0 + Math.max((baseRadius - 20)/20f, 0.5));
									if (inverseRadius < -0.375 * radius && inverseRadius >= ejectaRadius * radius)
										this.setBlockState(world, this.getBlockToPlace(world.getWorld(), x, z, ores, rand), x, y + dist + 1, z, bb);
									else if (inverseRadius < ejectaRadius * radius)
										this.setBlockState(world, this.getBlockToPlace(world.getWorld(), x, z, ores, rand), x, y + dist + 1 + rand.nextInt(2), z, bb);
								}
							}
						}

						//Places blocks to form the surface of the bowl
						if (inversePartialSquareRadius >= 0 && (y - inversePartialSquareRadius > 0)) {
							//Two blocks to remove weird stone
							int maximumDepth = craterType == CraterType.HUGE ? 28 : large ? 16 : 12;
							this.setBlockState(world, this.getBlockToPlace(world.getWorld(), x, z, ores, rand), x, y - Math.min(maximumDepth, inversePartialSquareRadius), z, bb);
							this.setBlockState(world, this.getBlockToPlace(world.getWorld(), x, z, ores, rand), x, y - 1 - Math.min(maximumDepth, inversePartialSquareRadius), z, bb);
						}

						if(centerSpire && blockRadius < 0.25D * radius) {
							int spireHeight = (int)Math.pow(Math.abs(-(radius / 16D) + blockRadius / 4D), 1.25D);
							for(int dist = 0; dist < spireHeight; dist++) {
								this.setBlockState(world, this.getBlockToPlaceRich(world.getWorld(), x, z, ores, rand),
										x, y + Math.min(dist, 16) - 27, z, bb);
							}
						}

						break;
					}
				}
			}
		}
		return true;
	}
	
   protected void setBlockState(ISeedReader worldIn, BlockState blockstateIn, int x, int y, int z, MutableBoundingBox boundingboxIn) {
	   //super.setBlockState(worldIn, blockstateIn, x, y, z, boundingboxIn);
	   worldIn.setBlockState(new BlockPos(x,y,z), blockstateIn, 2);
   }

	//Ignore liquids, and ignore air. Everything else is fair game
	private static boolean isCraterIgnoredBlock(Block block) {
		return block instanceof FlowingFluidBlock || block instanceof IFluidBlock || block == Blocks.AIR || block == Blocks.ICE;
	}

	//Place some ores but not a lot, if ore list exists
	private BlockState getBlockToPlace(World world, int x, int z, List<BlockState> ores, Random rand) {
		if(rand.nextInt(24) == 0 && !ores.isEmpty()) {
			return ores.get(rand.nextInt(ores.size()));
		} else {
			return world.getBiome(new BlockPos(x, 64, z)).getGenerationSettings().getSurfaceBuilderConfig().getTop();
		}
	}

	private BlockState getBlockToPlaceRich(World world, int x, int z, List<BlockState> ores, Random rand) {
		if(rand.nextInt(4) == 0 && !ores.isEmpty())
			return ores.get(rand.nextInt(ores.size()));
		return world.getBiome(new BlockPos(x, 64, z)).getGenerationSettings().getSurfaceBuilderConfig().getTop();
	}

	private static int chooseRadius(Random random, CraterType craterType, boolean airless) {
		if(craterType == CraterType.SMALL)
			return 4 + random.nextInt(4);
		if(craterType == CraterType.HUGE)
			return getHugeRadius(random.nextInt(400), random);
		return getNormalRadius(random.nextInt(500), random, airless);
	}

	private static int getNormalRadius(int randomValue, Random random, boolean largeCraters) {
		int radius = 8;
		if (randomValue < 440)
			radius += random.nextInt(16);
		else if (randomValue < 485)
			radius += 24 + random.nextInt(16);
		else if (largeCraters && randomValue < 495)
			radius += 40 + random.nextInt(16);
		else if (largeCraters && randomValue < 499)
			radius += 56 + random.nextInt(28);
		return radius;
	}

	private static int getHugeRadius(int randomValue, Random random) {
		int radius = 84;
		if(randomValue < 200)
			radius += random.nextInt(75);
		else if(randomValue < 325)
			radius += 24 + random.nextInt(75);
		else if(randomValue < 375)
			radius += 40 + random.nextInt(75);
		else
			radius += 56 + random.nextInt(75);
		return radius;
	}

	private static int getStructureDiameter(CraterType craterType) {
		switch(craterType) {
			case SMALL:
				return 64;
			case HUGE:
				return 1664;
			default:
				return 288;
		}
	}

	private long getShapeSeed() {
		return ((long)xCenter * 341873128712L)
				^ ((long)zCenter * 132897987541L)
				^ ((long)radius * 42317861L)
				^ craterType.ordinal();
	}

	//Very fun function for fancy radius
	//Int[] MUST be the same size as max bumps or larger!
	private int getRadius(int base, int x, int z, int bumps, int[] random) {
		//We need to start this out with polar coordinates
		double radians = Math.atan2(x, z);

		//Then we want to add some sin-function bumps to it, as determined by the bumps
		//They increase theta each time because then we can get different-placed perturbations
		//An example graph for this is here: https://www.desmos.com/calculator/5ojoqscuxv
		int extras = 0;
		for (int i = 2; i < Math.min(5, bumps) + 2; i++){
			extras += random[i-2] * base * Math.sin(i * radians) * 0.0075;
		}

		return base + extras;
	}
	
}
