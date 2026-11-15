package dev.epicshawty.lasttick;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LastTickClient implements ClientModInitializer {
	private static final Logger LOG = LoggerFactory.getLogger("lasttick");
	private static final int FIRE_LOG_LIMIT = 10;
	private static final int PROBE_LOG_INTERVAL = 20;
	private static KeyMapping toggleKey;
	private static int firesLogged;
	private static int ticks;
	private static boolean loggedBinding;

	@Override
	public void onInitializeClient() {
		Config.load(LOG);

		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.lasttick.toggle",
				InputConstants.Type.KEYBOARD,
				InputConstants.UNKNOWN.getValue(),
				KeyMapping.Category.MISC));

		ClientTickEvents.START_CLIENT_TICK.register(LastTickClient::onStartTick);
	}

	private static void onStartTick(Minecraft client) {
		// One channel only: two of them toggle the state twice per press and cancel out.
		while (toggleKey.consumeClick()) {
			Config.toggle(LOG, "key event");
		}

		if (client.player == null || client.level == null) {
			return;
		}

		if (!loggedBinding) {
			loggedBinding = true;
			InputConstants.Key bound = KeyMappingHelper.getBoundKeyOf(toggleKey);
			LOG.info("lasttick: bound={} value={} enabled={}", bound.getName(), bound.getValue(), Config.enabled());
		}

		ticks++;

		if (!Config.enabled()) {
			return;
		}

		LocalPlayer player = client.player;
		ClientLevel level = client.level;

		if (EdgeJump.shouldJump(player, level)) {
			Vec3 pos = player.position();
			player.jumpFromGround();

			if (firesLogged < FIRE_LOG_LIMIT) {
				firesLogged++;
				LOG.info("lasttick: jump at {}, {}, {}", round(pos.x), round(pos.y), round(pos.z));
			}

			return;
		}

		if (ticks % PROBE_LOG_INTERVAL == 0) {
			probe(player, level);
		}
	}

	/**
	 * One line per second while the player is walking on the ground: the gate values the predicate
	 * sees, and how far ahead the heading leaves support. {@code edge=-1} with no jumps ever logged
	 * means the collision query never reports air, i.e. the predicate cannot fire at all.
	 */
	private static void probe(LocalPlayer player, ClientLevel level) {
		Vec3 velocity = player.getDeltaMovement();
		double dx = velocity.x;
		double dz = velocity.z;

		if (!player.onGround() || dx * dx + dz * dz < 2.5E-3) {
			return;
		}

		AABB box = player.getBoundingBox();
		EdgeJump.BlockProbe probe = b -> level.getBlockCollisions(player, b).iterator().hasNext();
		boolean nextSupported = !EdgeJump.leavesSupport(box, dx, dz, probe);

		LOG.info("lasttick: probe spd={} mayfly={} crouch={} jumpHeld={} nextSupported={} edge={}",
				Math.round(Math.sqrt(dx * dx + dz * dz) * 1000.0),
				player.getAbilities().mayfly,
				player.isCrouching(),
				player.input.keyPresses.jump(),
				nextSupported,
				EdgeJump.edgeDistance(box, dx, dz, 3.0, probe));
	}

	private static long round(double value) {
		return Math.round(value * 10.0);
	}
}
