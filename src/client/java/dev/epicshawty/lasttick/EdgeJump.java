package dev.epicshawty.lasttick;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * True only on the last grounded tick before the next tick's swept box has no block under it.
 *
 * <p>The next move is exactly {@code pos + deltaMovement} (vanilla applies no other horizontal
 * impulse after friction), so the box translated by that vector is the position the player will
 * occupy one tick from now. If nothing collides under that box, this is the last tick that can
 * jump: {@code jumpFromGround} requires being grounded.
 */
public final class EdgeJump {
	/** Below this the player is stationary, not leaving anything. Squared, so 0.05 blocks/tick. */
	private static final double MIN_SPEED_SQ = 2.5E-3;
	/** Block tops sit at integer heights; any epsilon below the feet finds them. */
	private static final double GROUND_PROBE = 0.0625;
	/** Keeps a block face that only touches the box edge from counting as floor. */
	private static final double HORIZONTAL_EPSILON = 1.0E-3;

	private EdgeJump() {
	}

	public static boolean shouldJump(LocalPlayer player, ClientLevel level) {
		if (!player.onGround()) {
			return false;
		}

		if (player.isSpectator() || player.isPassenger()) {
			return false;
		}

		if (player.isFallFlying() || player.isInWater() || player.isInLava() || player.onClimbable()) {
			return false;
		}

		// Sneaking keeps the player from walking off. Jumping there would be a surprise, not an assist.
		if (player.isCrouching() || player.isShiftKeyDown()) {
			return false;
		}

		Abilities abilities = player.getAbilities();

		// Creative: flight is one tap away, and a mod that jumps you in and out of it is a hazard.
		if (abilities.flying || abilities.mayfly) {
			return false;
		}

		// The player already jumped this tick; a second call would apply the sprint boost twice.
		if (player.input.keyPresses.jump()) {
			return false;
		}

		Vec3 velocity = player.getDeltaMovement();
		double dx = velocity.x;
		double dz = velocity.z;

		if (dx * dx + dz * dz < MIN_SPEED_SQ) {
			return false;
		}

		return leavesSupport(player.getBoundingBox(), dx, dz, box -> level.getBlockCollisions(player, box).iterator().hasNext());
	}

	/** True when the box, moved by one tick of horizontal velocity, has nothing under it. */
	public static boolean leavesSupport(AABB box, double dx, double dz, BlockProbe probe) {
		AABB next = box.move(dx, 0.0, dz)
				.move(0.0, -GROUND_PROBE, 0.0)
				.deflate(HORIZONTAL_EPSILON, 0.0, HORIZONTAL_EPSILON);
		return !probe.blocked(next);
	}

	/** Collision query, kept separate so the prediction can be checked without a Level. */
	public interface BlockProbe {
		boolean blocked(AABB box);
	}
}
