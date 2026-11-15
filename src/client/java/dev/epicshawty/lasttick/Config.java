package dev.epicshawty.lasttick;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Single field, read once at start and written on every toggle. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static boolean enabled;

	private Config() {
	}

	public static boolean enabled() {
		return enabled;
	}

	public static void toggle(Logger log) {
		enabled = !enabled;
		save(log);
	}

	public static void load(Logger log) {
		Path file = file();

		if (!Files.isRegularFile(file)) {
			save(log);
			return;
		}

		try {
			Data data = GSON.fromJson(Files.readString(file), Data.class);
			enabled = data != null && data.enabled;
		} catch (IOException | RuntimeException e) {
			log.warn("lasttick: config unreadable, staying off", e);
			enabled = false;
		}
	}

	private static void save(Logger log) {
		Path file = file();

		try {
			Files.createDirectories(file.getParent());
			Files.writeString(file, GSON.toJson(new Data(enabled)));
		} catch (IOException e) {
			log.warn("lasttick: config not saved", e);
		}
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("lasttick.json");
	}

	private static final class Data {
		boolean enabled;

		Data() {
		}

		Data(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
