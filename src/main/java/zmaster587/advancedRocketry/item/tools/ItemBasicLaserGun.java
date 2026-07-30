package zmaster587.advancedRocketry.item.tools;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.UseAction;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.util.AudioRegistry;
import zmaster587.libVulpes.LibVulpes;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.WeakHashMap;

public class ItemBasicLaserGun extends Item {

	private static final int REACH_DISTANCE = 50;
	private final WeakHashMap<LivingEntity, BlockPos> posMap;
	private final ItemTier toolMaterial;

	public ItemBasicLaserGun( Properties props ) {
		super(props);
		toolMaterial = ItemTier.DIAMOND;
		posMap = new WeakHashMap<>();
	}
	
	public boolean canHarvestBlock(BlockState blockIn)
	{
		Block block = blockIn.getBlock();

		if (block == Blocks.OBSIDIAN)
		{
			return this.toolMaterial.getHarvestLevel() == 3;
		}
		else if (block != Blocks.DIAMOND_BLOCK && block != Blocks.DIAMOND_ORE)
		{
			if (block != Blocks.EMERALD_ORE && block != Blocks.EMERALD_BLOCK)
			{
				if (block != Blocks.GOLD_BLOCK && block != Blocks.GOLD_ORE)
				{
					if (block != Blocks.IRON_BLOCK && block != Blocks.IRON_ORE)
					{
						if (block != Blocks.LAPIS_BLOCK && block != Blocks.LAPIS_ORE)
						{
							if (block != Blocks.REDSTONE_ORE)
							{
								Material material = blockIn.getMaterial();
								return material == Material.ROCK || material == Material.IRON || material == Material.ANVIL;
							}
							else
							{
								return this.toolMaterial.getHarvestLevel() >= 2;
							}
						}
						else
						{
							return this.toolMaterial.getHarvestLevel() >= 1;
						}
					}
					else
					{
						return this.toolMaterial.getHarvestLevel() >= 1;
					}
				}
				else
				{
					return this.toolMaterial.getHarvestLevel() >= 2;
				}
			}
			else
			{
				return this.toolMaterial.getHarvestLevel() >= 2;
			}
		}
		else
		{
			return this.toolMaterial.getHarvestLevel() >= 2;
		}
	}


	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {

		World world = player.getEntityWorld();

		RayTraceResult rayTrace = rayTraceEntity(world,player);

		if(rayTrace instanceof EntityRayTraceResult) {
			if(!world.isRemote)
				((EntityRayTraceResult)rayTrace).getEntity().attackEntityFrom(DamageSource.GENERIC, 1f);
			playSound(world, player);
			AdvancedRocketry.proxy.spawnLaser(player, rayTrace.getHitVec());
			player.resetActiveHand();
			return;
		}

		if (!(player instanceof PlayerEntity))
			return;
		rayTrace = rayTrace(world, (PlayerEntity) player);

		if(rayTrace == null)
			return;

		BlockPos hitPos = ((BlockRayTraceResult)rayTrace).getPos();
		if(posMap.get(player) != null && !posMap.get(player).equals(hitPos)) {
			player.resetActiveHand();
			return;
		}
		else if(posMap.get(player) == null) {
			posMap.put(player, hitPos);
		}

		if(rayTrace.getType() == Type.BLOCK) {
			if(count % 5 == 0 && world.isRemote)
				playSound(world, player);
			AdvancedRocketry.proxy.spawnLaser(player, rayTrace.getHitVec());
			super.onUsingTick(stack, player, count);
		}
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 16;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.NONE;
	}

	protected RayTraceResult rayTrace(World worldIn, PlayerEntity playerIn) {
		float f = playerIn.rotationPitch;
		float f1 = playerIn.rotationYaw;
		double d0 = playerIn.getPosX();
		double d1 = playerIn.getPosY() + (double)playerIn.getEyeHeight();
		double d2 = playerIn.getPosZ();
		Vector3d vec3d = new Vector3d(d0, d1, d2);
		float f2 = MathHelper.cos(-f1 * 0.017453292F - (float)Math.PI);
		float f3 = MathHelper.sin(-f1 * 0.017453292F - (float)Math.PI);
		float f4 = -MathHelper.cos(-f * 0.017453292F);
		float f5 = MathHelper.sin(-f * 0.017453292F);
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d3 = REACH_DISTANCE;

		Vector3d vec3d1 = vec3d.add((double)f6 * d3, (double)f5 * d3, (double)f7 * d3);
		
		return worldIn.rayTraceBlocks(new RayTraceContext(vec3d, vec3d1, BlockMode.COLLIDER, FluidMode.NONE, playerIn));
	}

	@Nonnull
	@Override
	@ParametersAreNonnullByDefault
	public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entityLiving) {
		if (!(entityLiving instanceof PlayerEntity)) {
			posMap.remove(entityLiving);
			return stack;
		}
		RayTraceResult rayTrace = rayTrace(world, (PlayerEntity) entityLiving);

		if(rayTrace != null && rayTrace.getType() == Type.BLOCK) {
			BlockPos hitPos = ((BlockRayTraceResult)rayTrace).getPos();
			BlockState state = world.getBlockState(hitPos);
			if(state.getBlockHardness(world, hitPos) != -1) {

				//
				if(!world.isRemote) {
					((ServerPlayerEntity)entityLiving).interactionManager.tryHarvestBlock(hitPos);
				}

				//state.getPlayerRelativeBlockHardness((PlayerEntity)player, world, rayTrace.getBlockPos());
			}
		}

		posMap.remove(entityLiving);

		return stack;
	}

	public RayTraceResult rayTraceEntity(World world, Entity entity) {

		Vector3d vec3d = new Vector3d(entity.getPosX(), entity.getPosY() + entity.getEyeHeight(), entity.getPosZ());
		Vector3d vec3d1 = entity.getLook(0);
		Vector3d vec3d2 = vec3d.add(vec3d1.scale(REACH_DISTANCE));
		AxisAlignedBB searchBox = entity.getBoundingBox().expand(vec3d1.scale(REACH_DISTANCE)).grow(1.0D);
		return ProjectileHelper.rayTraceEntities(world, entity, vec3d, vec3d2, searchBox,
				target -> !target.isSpectator() && target.canBeCollidedWith());
	}



	@Nonnull
	@Override
	@ParametersAreNonnullByDefault
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand hand) {

		player.setActiveHand(hand);

		posMap.remove(player);
		ItemStack stack = player.getHeldItem(hand);

		World world = player.getEntityWorld();

		RayTraceResult rayTrace = rayTraceEntity(world,player);

		if(rayTrace instanceof EntityRayTraceResult) {
			if (!world.isRemote)
				((EntityRayTraceResult)rayTrace).getEntity().attackEntityFrom(DamageSource.GENERIC, .5f);
			playSound(world, player);
			AdvancedRocketry.proxy.spawnLaser(player, rayTrace.getHitVec());
			return ActionResult.resultConsume(stack);
		}

		rayTrace = rayTrace(world, player);

		if(rayTrace != null && rayTrace.getType() == Type.BLOCK) {
			playSound(world, player);
			AdvancedRocketry.proxy.spawnLaser(player, rayTrace.getHitVec());
			return ActionResult.resultConsume(stack);
		}
		return ActionResult.resultPass(stack);
	}

	private void playSound(World world, LivingEntity player) {
		if (!world.isRemote)
			world.playSound(null, player.getPosition(), AudioRegistry.basicLaser,
					SoundCategory.PLAYERS, 1.0f, 1.0f);
	}
}
