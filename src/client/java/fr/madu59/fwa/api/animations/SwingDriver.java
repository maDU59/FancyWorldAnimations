package fr.madu59.fwa.api.animations;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/*
 * SwingDriver interface supplies the swing of lanterns and chains, replacing the idle
 * animation. Called once per block per rendered frame, including the shadow pass, so it must
 * return the same swing for a given position and tick instead of advancing its own sim.
 */
@FunctionalInterface
public interface SwingDriver {

    boolean getSwing(BlockPos position, BlockState state, double nowTick, HangingSwing swing);
}
