package zmaster587.advancedRocketry.world.decoration;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.structure.ScatteredStructurePiece;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
import zmaster587.libVulpes.util.ZUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StructurePieceGeode extends ScatteredStructurePiece {
	int radius;
	int xCenter, zCenter;

	public static void init() {
		// Kept as a compatibility entry point. Ore lists are now assembled per
		// generation call so config reloads and dimension overrides stay local.
	}
	
	public StructurePieceGeode(Random random, int x, int z) {
		this(random, x, z, chooseRadius(random));
	}

	private StructurePieceGeode(Random random, int x, int z, int radius) {
		super(AdvancedRocketryBiomes.STRUCTURE_PIECE_GEODE, random, x, 64, z,
				radius * 2 + 1, Math.max(15, radius + 1), radius * 2 + 1);
		this.setCoordBaseMode(null);
		this.radius = radius;
		xCenter = x;
		zCenter = z;
	}

	private static int chooseRadius(Random random) {
		int variation = Math.max(0, ARConfiguration.getCurrentConfig().geodeVariation.get());
		int radius = ARConfiguration.getCurrentConfig().geodeBaseSize.get() - variation / 2;
		if(variation > 0)
			radius += random.nextInt(variation);
		return Math.max(4, radius);
	}

    public StructurePieceGeode(TemplateManager mgr, CompoundNBT piece) {
        super(AdvancedRocketryBiomes.STRUCTURE_PIECE_GEODE, piece);
        radius = piece.getInt("Radius");
        xCenter = piece.getInt("xCenter");
        zCenter =  piece.getInt("zCenter");
     }
	

    /**
     * (abstract) Helper method to read subclass data from NBT
     */
    protected void readAdditional(CompoundNBT tagCompound) {
       super.readAdditional(tagCompound);
       tagCompound.putInt("Radius", radius);
       tagCompound.putInt("xCenter", xCenter);
       tagCompound.putInt("zCenter", zCenter);
    }

	public boolean func_230383_a_(ISeedReader world, StructureManager structureMgr, ChunkGenerator chunkGen, Random rand, MutableBoundingBox bb, ChunkPos chunkPos, BlockPos blockPos) {
		int depth = radius*radius;

		int chunkX = chunkPos.x;
		int chunkZ = chunkPos.z;
		
		int xCoord = (chunkX << 4) - xCenter - radius;
		int zCoord =  (chunkZ << 4) - zCenter - radius;

		DimensionProperties props = DimensionManager.getInstance().getDimensionProperties(ZUtils.getDimensionIdentifier(world.getWorld()));
		List<BlockState> ores = new ArrayList<>();
		for(Block block : ARConfiguration.getCurrentConfig().standardGeodeOres) {
			BlockState state = block.getDefaultState();
			if(!ores.contains(state))
				ores.add(state);
		}
		for(net.minecraft.item.ItemStack stack : props.geodeOres) {
			BlockState state = Block.getBlockFromItem(stack.getItem()).getDefaultState();
			if(state.getBlock() != Blocks.AIR && !ores.contains(state))
				ores.add(state);
		}
		BlockState fallbackOre = ores.isEmpty() ? Blocks.STONE.getDefaultState() : null;

		int avgY = 64;

		for(int relx = 15; relx >= 0; relx--) {
			for(int relz = 15; relz >= 0; relz--) {

				int count = ( depth - ( ((xCoord)+relx)*((xCoord)+relx) + ((zCoord)+relz)*((zCoord)+relz) ) )/(radius*2);

				int x = (chunkX << 4) + relx;
				int z = (chunkZ << 4) + relz;
				
				//Check for IOB exceptions early, in case it generates near bedrock or something
				if(avgY-count < 1 || avgY+count > 255)
					continue;

				//Clears air for the ceiling
				for(int dist = -count; dist < Math.min(count,3); dist++) {
					setBlockState(world, Blocks.AIR.getDefaultState(), x, avgY - dist, z, bb);
				}

				if(count >= 0) {

					if(count > 4) {
						int size = rand.nextInt(4) + 4;

						//Generates ore hanging from the ceiling
						if( relx % 4 > 0 && relz % 4 > 0) {
							BlockState ore = fallbackOre == null ? ores.get((relx/4 + relz/4) % ores.size()) : fallbackOre;
							for(int i = 1; i < size; i++)
								setBlockState(world, ore, x, avgY + count - i, z, bb);
						}
						else {
							size -=2;
							for(int i = 1; i < size; i++) {
								setBlockState(world, Blocks.STONE.getDefaultState(), x, avgY + count - i, z, bb);
							}
						}

						//Generates ore in the floor
						if( (relx+2) % 4 > 0 && (relz+2) % 4 > 0) {
							BlockState ore = fallbackOre == null ? ores.get((relx/4 + relz/4) % ores.size()) : fallbackOre;
							for(int i = 1; i < size; i++)
								setBlockState(world, ore, x, avgY - count + i, z, bb);
						}

					}

					setBlockState(world, AdvancedRocketryBlocks.blockGeode.getDefaultState(), x, avgY - count, z, bb);
					setBlockState(world, AdvancedRocketryBlocks.blockGeode.getDefaultState(), x, avgY + count, z, bb);
				}
			}
		}
		return true;
	}

   protected void setBlockState(ISeedReader worldIn, BlockState blockstateIn, int x, int y, int z, MutableBoundingBox boundingboxIn) {
	   //super.setBlockState(worldIn, blockstateIn, x, y, z, boundingboxIn);
	   worldIn.setBlockState(new BlockPos(x,y,z), blockstateIn, 2);
   }
}
