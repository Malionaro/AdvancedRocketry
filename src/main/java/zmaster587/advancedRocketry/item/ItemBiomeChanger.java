package zmaster587.advancedRocketry.item;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import zmaster587.advancedRocketry.AdvancedRocketry;
import zmaster587.advancedRocketry.api.AdvancedRocketryBiomes;
import zmaster587.advancedRocketry.api.SatelliteRegistry;
import zmaster587.advancedRocketry.api.satellite.SatelliteBase;
import zmaster587.advancedRocketry.network.PacketSatellite;
import zmaster587.advancedRocketry.satellite.SatelliteBiomeChanger;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.api.LibvulpesGuiRegistry;
import zmaster587.libVulpes.inventory.ContainerModular;
import zmaster587.libVulpes.inventory.GuiHandler;
import zmaster587.libVulpes.inventory.modules.IButtonInventory;
import zmaster587.libVulpes.inventory.modules.IModularInventory;
import zmaster587.libVulpes.inventory.modules.ModuleBase;
import zmaster587.libVulpes.inventory.modules.ModuleButton;
import zmaster587.libVulpes.inventory.modules.ModuleContainerPan;
import zmaster587.libVulpes.inventory.modules.ModuleImage;
import zmaster587.libVulpes.inventory.modules.ModulePower;
import zmaster587.libVulpes.network.INetworkItem;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketItemModifcation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class ItemBiomeChanger extends ItemSatelliteChip implements IModularInventory, IButtonInventory, INetworkItem {

	private static final byte BUTTON_SCAN = -1;
	private static final byte BUTTON_SELECT = 0;

	public ItemBiomeChanger(Properties properties) {
		super(properties);
	}

	private SatelliteBiomeChanger getBiomeSatellite(ItemStack stack) {
		SatelliteBase satellite = getSatellite(stack);
		return satellite instanceof SatelliteBiomeChanger ? (SatelliteBiomeChanger) satellite : null;
	}

	@Override
	public List<ModuleBase> getModules(int id, PlayerEntity player) {
		List<ModuleBase> modules = new LinkedList<>();
		ItemStack stack = player.getHeldItemMainhand().getItem() == this
				? player.getHeldItemMainhand() : player.getHeldItemOffhand();
		SatelliteBiomeChanger satellite = getBiomeSatellite(stack);
		if(satellite == null)
			return modules;

		if(player.world.isRemote)
			modules.add(new ModuleImage(24, 14, zmaster587.advancedRocketry.inventory.TextureResources.earthCandyIcon));

		List<ModuleBase> biomeButtons = new LinkedList<>();
		int row = 0;
		for(ResourceLocation biomeId : satellite.getDiscoveredBiomes()) {
			Biome biome = AdvancedRocketryBiomes.getBiomeFromResourceLocation(biomeId);
			if(biome == null)
				continue;

			ModuleButton button = new ModuleButton(32, 16 + 24 * row++, AdvancedRocketry.proxy.getNameFromBiome(biome), this, zmaster587.libVulpes.inventory.TextureResources.buttonBuild);
			button.setAdditionalData(biomeId);
			if(biome == satellite.getBiome())
				button.setColor(0xFF22FF22);
			biomeButtons.add(button);
		}

		modules.add(new ModuleContainerPan(32, 16, biomeButtons, new LinkedList<>(), null, 128, 128, 0, -64, 0, 1000));
		ModuleButton scan = new ModuleButton(120, 124, LibVulpes.proxy.getLocalizedString("msg.biomechanger.scan"), this, zmaster587.libVulpes.inventory.TextureResources.buttonScan);
		scan.setAdditionalData(BUTTON_SCAN);
		modules.add(scan);
		modules.add(new ModulePower(16, 48, satellite.getBattery()));
		return modules;
	}

	@Override
	@ParametersAreNonnullByDefault
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
		SatelliteBiomeChanger satellite = getBiomeSatellite(stack);
		if(!stack.hasTag())
			list.add(new StringTextComponent(LibVulpes.proxy.getLocalizedString("msg.unprogrammed")));
		else if(satellite == null)
			list.add(new StringTextComponent(LibVulpes.proxy.getLocalizedString("msg.biomechanger.nosat")));
		else if(world != null && satellite.getDimensionId().isPresent()
				&& satellite.getDimensionId().get().equals(world.getDimensionKey().getLocation())) {
			list.add(new StringTextComponent(LibVulpes.proxy.getLocalizedString("msg.connected")));
			ResourceLocation selected = satellite.getBiome() == null ? null : AdvancedRocketryBiomes.getBiomeResource(satellite.getBiome());
			list.add(new StringTextComponent(LibVulpes.proxy.getLocalizedString("msg.biomechanger.selbiome") + " " + (selected == null ? "-" : selected)));
			list.add(new StringTextComponent(LibVulpes.proxy.getLocalizedString("msg.biomechanger.numbiome") + " " + satellite.getDiscoveredBiomes().size()));
		}
		else
			list.add(new StringTextComponent(LibVulpes.proxy.getLocalizedString("msg.notconnected")));

		super.addInformation(stack, world, list, flag);
	}

	@Override
	@Nonnull
	@ParametersAreNonnullByDefault
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(!world.isRemote) {
			SatelliteBiomeChanger satellite = getBiomeSatellite(stack);
			if(satellite != null) {
				if(player.isSneaking()) {
					PacketHandler.sendToPlayer(new PacketSatellite(satellite), player);
					NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) stack.getItem(),
							buffer -> {
								buffer.writeInt(getModularInvType().ordinal());
								buffer.writeBoolean(hand == Hand.MAIN_HAND);
							});
				}
				else {
					satellite.performAction(player, world, player.getPosition());
				}
			}
		}
		return ActionResult.resultSuccess(stack);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onInventoryButtonPressed(ModuleButton button) {
		ItemStack stack = Minecraft.getInstance().player.getHeldItemMainhand().getItem() == this
				? Minecraft.getInstance().player.getHeldItemMainhand()
				: Minecraft.getInstance().player.getHeldItemOffhand();
		if(stack.isEmpty() || stack.getItem() != this)
			return;

		Object data = button.getAdditionalData();
		if(data instanceof ResourceLocation) {
			SatelliteBiomeChanger satellite = getBiomeSatellite(stack);
			if(satellite != null)
				satellite.setBiome(AdvancedRocketryBiomes.getBiomeFromResourceLocation((ResourceLocation) data));
			PacketHandler.sendToServer(new PacketItemModifcation(this, Minecraft.getInstance().player, BUTTON_SELECT));
		}
		else if(data instanceof Byte && (Byte) data == BUTTON_SCAN) {
			PacketHandler.sendToServer(new PacketItemModifcation(this, Minecraft.getInstance().player, BUTTON_SCAN));
		}
	}

	@Override
	public void writeDataToNetwork(ByteBuf out, byte id, @Nonnull ItemStack stack) {
		if(id != BUTTON_SELECT)
			return;

		SatelliteBiomeChanger satellite = getBiomeSatellite(stack);
		ResourceLocation biomeId = satellite == null || satellite.getBiome() == null
				? null
				: AdvancedRocketryBiomes.getBiomeResource(satellite.getBiome());
		byte[] bytes = (biomeId == null ? "" : biomeId.toString()).getBytes(StandardCharsets.UTF_8);
		out.writeShort(bytes.length);
		out.writeBytes(bytes);
	}

	@Override
	public void readDataFromNetwork(ByteBuf in, byte id, CompoundNBT nbt, ItemStack stack) {
		if(id == BUTTON_SELECT) {
			int length = in.readUnsignedShort();
			byte[] bytes = new byte[length];
			in.readBytes(bytes);
			nbt.putString("biome", new String(bytes, StandardCharsets.UTF_8));
		}
	}

	@Override
	public void useNetworkData(PlayerEntity player, Dist side, byte id, CompoundNBT nbt, ItemStack stack) {
		if(player.world.isRemote)
			return;

		SatelliteBiomeChanger satellite = getBiomeSatellite(stack);
		if(satellite == null)
			return;

		if(id == BUTTON_SCAN) {
			satellite.addBiome(player.world.getBiome(player.getPosition()));
		}
		else if(id == BUTTON_SELECT) {
			ResourceLocation biomeId = ResourceLocation.tryCreate(nbt.getString("biome"));
			if(biomeId != null && satellite.getDiscoveredBiomes().contains(biomeId))
				satellite.setBiome(AdvancedRocketryBiomes.getBiomeFromResourceLocation(biomeId));
		}
		player.closeScreen();
	}

	@Override
	public String getModularInventoryName() {
		return "item.advancedrocketry.biomechangerremote";
	}

	@Override
	public boolean canInteractWithContainer(PlayerEntity player) {
		return player.getHeldItemMainhand().getItem() == this || player.getHeldItemOffhand().getItem() == this;
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(getModularInventoryName());
	}

	@Override
	@ParametersAreNonnullByDefault
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
		return new ContainerModular(LibvulpesGuiRegistry.CONTAINER_MODULAR_HELD_ITEM, id, player,
				getModules(getModularInvType().ordinal(), player), this, getModularInvType());
	}

	@Override
	public GuiHandler.guiId getModularInvType() {
		return GuiHandler.guiId.MODULARNOINV;
	}
}
