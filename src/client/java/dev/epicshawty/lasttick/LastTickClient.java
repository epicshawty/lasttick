package dev.epicshawty.lasttick;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LastTickClient implements ClientModInitializer {
	private static final Logger LOG = LoggerFactory.getLogger("lasttick");
	private static KeyMapping toggleKey;

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
		while (toggleKey.consumeClick()) {
			Config.toggle(LOG);
		}

		if (!Config.enabled() || client.player == null || client.level == null) {
			return;
		}

		if (EdgeJump.shouldJump(client.player, client.level)) {
			client.player.jumpFromGround();
		}
	}
}
