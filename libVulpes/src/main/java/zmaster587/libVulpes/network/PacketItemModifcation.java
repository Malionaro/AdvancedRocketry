package zmaster587.libVulpes.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PacketItemModifcation extends BasePacket {
	
	CompoundNBT nbt;

	byte packetId;
	int entityId;
	PlayerEntity entity;
	INetworkItem machine;
	Hand hand = Hand.MAIN_HAND;

	public PacketItemModifcation() {
		nbt = new CompoundNBT();
	}

	public PacketItemModifcation(INetworkItem machine, PlayerEntity entity, byte packetId) {
		this();
		this.machine = machine;
		this.entity = entity;
		this.packetId = packetId;
		this.entityId = entity.getEntityId();
		if(entity.getHeldItem(Hand.MAIN_HAND).getItem() != machine
				&& entity.getHeldItem(Hand.OFF_HAND).getItem() == machine)
			this.hand = Hand.OFF_HAND;
	}


	public PacketItemModifcation(INetworkItem machine, PlayerEntity entity, byte packetId, CompoundNBT nbt) {
		this(machine, entity, packetId);
		this.nbt = nbt;
	}

	@Override
	public void write(PacketBuffer out) {
		BasePacket.writeWorld(out, entity.world);
		out.writeInt(entity.getEntityId());
		out.writeByte(packetId);
		out.writeBoolean(hand == Hand.MAIN_HAND);

		out.writeBoolean(!nbt.isEmpty());

		if(!nbt.isEmpty()) {
			out.writeCompoundTag(nbt);
		}

		machine.writeDataToNetwork(out, packetId, entity.getHeldItem(Hand.MAIN_HAND));
	}

	@Override
	public void read(PacketBuffer in) {
		//DEBUG:
		World world = BasePacket.readWorld(in);

		int entityId = in.readInt();
		packetId = in.readByte();
		hand = in.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;

		Entity ent = world.getEntityByID(entityId);
		if(ent == null) {
			for(Entity e : world.getPlayers()) {
				if(e.getEntityId() == entityId) {
					ent = e;
					break;
				}
			}
		}

		if(in.readBoolean()) {
			CompoundNBT nbt;
			nbt = in.readCompoundTag();
			this.nbt = nbt;
		}

		if(ent instanceof PlayerEntity) {
			ItemStack itemStack = ((PlayerEntity)ent).getHeldItem(hand);
			if(!itemStack.isEmpty() && itemStack.getItem() instanceof INetworkItem) {
				((INetworkItem)itemStack.getItem()).readDataFromNetwork(in, packetId, nbt, itemStack);
			}
		}
	}

	public void execute(PlayerEntity player, Dist side) {
		if(player != null) {
			ItemStack itemStack = player.getHeldItem(hand);
			if(!itemStack.isEmpty() && itemStack.getItem() instanceof INetworkItem) {
				((INetworkItem)itemStack.getItem()).useNetworkData(player, side, packetId, nbt, itemStack);
			}
		}
	}

	@Override
	public void executeServer(ServerPlayerEntity player) {
		execute(player, Dist.DEDICATED_SERVER);
	}

	@Override
	public void executeClient(PlayerEntity player) {
		execute(player, Dist.CLIENT);
	}

	@OnlyIn(value=Dist.CLIENT)
	@Override
	public void readClient(PacketBuffer in) {
		PacketBuffer buffer = new PacketBuffer(in);

		//DEBUG:
		World world;

		BasePacket.readWorld(in);
		world = Minecraft.getInstance().world;

		int entityId = buffer.readInt();
		packetId = buffer.readByte();
		hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;

		Entity ent = world.getEntityByID(entityId);

		if(buffer.readBoolean()) {
			CompoundNBT nbt;
			nbt = buffer.readCompoundTag();
			this.nbt = nbt;
		}

		if(ent instanceof PlayerEntity) {
			ItemStack itemStack = ((PlayerEntity)ent).getHeldItem(hand);
			if(!itemStack.isEmpty() && itemStack.getItem() instanceof INetworkItem) {
				((INetworkItem)itemStack.getItem()).readDataFromNetwork(in, packetId, nbt, itemStack);
			}
		}
	}
}
