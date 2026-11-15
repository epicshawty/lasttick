package dev.epicshawty.lasttick;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LastTickClient implements ClientModInitializer {
	private static final Logger LOG = LoggerFactory.getLogger("lasttick");
	private static final int FIRE_LOG_LIMIT = 10;
	private static KeyMapping toggleKey;
	private static boolean toggleWasDown;
	private static int firesLogged;

	@Override
	public void onInitializeClient() {
		Config.load(LOG);

		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.lasttick.toggle",
				InputConstants.Type.KEYBOARD,
				InputConstants.UNKNOWN.getValue(),
				KeyMapping.Category.MISC));

		InputConstants.Key bound = boundKey();
		LOG.info("lasttick: ready, bound={} type={} value={} enabled={}",
				bound.getName(), bound.getType(), bound.getValue(), Config.enabled());

		ClientTickEvents.START_CLIENT_TICK.register(LastTickClient::onStartTick);
	}

	private static void onStartTick(Minecraft client) {
		pollToggle(client);

		if (!Config.enabled() || client.player == null || client.level == null) {
			return;
		}

		if (EdgeJump.shouldJump(client.player, client.level)) {
			Vec3 pos = client.player.position();
			client.player.jumpFromGround();

			if (firesLogged < FIRE_LOG_LIMIT) {
				firesLogged++;
				LOG.info("lasttick: jump at {}, {}, {}", round(pos.x), round(pos.y), round(pos.z));
			}
		}
	}

	/**
	 * Toggles on a raw key edge while in-game.
	 *
	 * <p>{@code KeyMapping.consumeClick()} is kept as a second channel, but it cannot be the only one:
	 * 26.3's {@code KeyMapping.setKey} assigns the bound key without re-registering the mapping in the
	 * private key-to-mappings index, so a mapping created unbound (as this one is) never receives clicks
	 * after the user binds it. {@link InputConstants#isKeyDown(int)} reads the same SDL keyboard state
	 * vanilla reads, indexed by the code the mapping holds.
	 */
	private static void pollToggle(Minecraft client) {
		while (toggleKey.consumeClick()) {
			Config.toggle(LOG, "key event");
		}

		boolean down = client.gui != null && client.gui.screen() == null && keyHeld();

		if (down && !toggleWasDown) {
			Config.toggle(LOG, "key state");
		}

		toggleWasDown = down;
	}

	private static boolean keyHeld() {
		InputConstants.Key key = boundKey();

		if (key.getType() == InputConstants.Type.KEYBOARD) {
			int code = key.getValue();
			return code >= 0 && code < 512 && InputConstants.isKeyDown(code);
		}

		return toggleKey.isDown();
	}

	private static InputConstants.Key boundKey() {
		InputConstants.Key key = KeyMappingHelper.getBoundKeyOf(toggleKey);
		return key == null ? InputConstants.UNKNOWN : key;
	}

	private static long round(double value) {
		return Math.round(value * 10.0);
	}
}
