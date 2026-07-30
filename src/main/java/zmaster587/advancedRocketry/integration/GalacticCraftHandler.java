package zmaster587.advancedRocketry.integration;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import zmaster587.advancedRocketry.AdvancedRocketry;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Optional Galacticraft oxygen bridge without a hard dependency on an
 * unavailable 1.16.5 Galacticraft API artifact.
 */
public final class GalacticCraftHandler {

	private static final String LEGACY_OXYGEN_EVENT =
			"micdoodle8.mods.galacticraft.api.event.oxygen.GCCoreOxygenSuffocationEvent$Pre";
	private static final String LEGACY_PLAYER_STATS =
			"micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats";
	private static final String LEGACY_CLIENT_STATS =
			"micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStatsClient";
	private static boolean registered;

	private GalacticCraftHandler() {
	}

	public static boolean isLoaded() {
		return ModList.get().isLoaded("galacticraftcore")
				|| ModList.get().isLoaded("galacticraft");
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void register() {
		if(registered || !isLoaded())
			return;

		try {
			Class<?> eventClass = Class.forName(LEGACY_OXYGEN_EVENT);
			if(Event.class.isAssignableFrom(eventClass)) {
				MinecraftForge.EVENT_BUS.addListener(
						EventPriority.HIGHEST,
						true,
						(Class<? extends Event>)eventClass,
						event -> cancelGalacticraftSuffocation((Event)event));
				registered = true;
				DistExecutor.runWhenOn(Dist.CLIENT, () -> ClientHooks::register);
				AdvancedRocketry.logger.info("Galacticraft oxygen compatibility enabled");
			}
		}
		catch(ClassNotFoundException exception) {
			AdvancedRocketry.logger.warn(
					"Galacticraft was detected, but its oxygen event API is not compatible with the 1.12 bridge");
		}
	}

	private static void cancelGalacticraftSuffocation(Event event) {
		if(event.isCancelable())
			event.setCanceled(true);

		Object entity = invokeNoArgs(event, "getEntity");
		if(entity != null)
			setBooleanStat(LEGACY_PLAYER_STATS, entity, "setLastOxygenSetupValid");
	}

	private static Object invokeNoArgs(Object target, String methodName) {
		try {
			return target.getClass().getMethod(methodName).invoke(target);
		}
		catch(ReflectiveOperationException exception) {
			return null;
		}
	}

	private static void setBooleanStat(String statsClassName, Object player, String setterName) {
		try {
			Class<?> statsClass = Class.forName(statsClassName);
			Object stats = null;
			for(Method method : statsClass.getMethods()) {
				if(method.getName().equals("get")
						&& Modifier.isStatic(method.getModifiers())
						&& method.getParameterCount() == 1
						&& method.getParameterTypes()[0].isInstance(player)) {
					stats = method.invoke(null, player);
					break;
				}
			}
			if(stats != null)
				stats.getClass().getMethod(setterName, boolean.class).invoke(stats, true);
		}
		catch(ReflectiveOperationException ignored) {
			// Galacticraft forks may omit the visual/stat bookkeeping. Canceling
			// the suffocation event still prevents duplicate oxygen damage.
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static final class ClientHooks {

		private static void register() {
			MinecraftForge.EVENT_BUS.addListener(ClientHooks::clientTick);
		}

		private static void clientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
			if(event.phase == net.minecraftforge.event.TickEvent.Phase.END
					&& net.minecraft.client.Minecraft.getInstance().player != null) {
				setBooleanStat(
						LEGACY_CLIENT_STATS,
						net.minecraft.client.Minecraft.getInstance().player,
						"setOxygenSetupValid");
			}
		}
	}
}
