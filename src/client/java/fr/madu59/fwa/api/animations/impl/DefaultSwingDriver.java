package fr.madu59.fwa.api.animations.impl;

import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultSwingDriver implements SwingDriver{
    public boolean getSwing(BlockPos position, BlockState state, double nowTick, HangingSwing swing){
        float posOffset = (position.getX() * 0.6f) + (position.getZ() * 0.6f);
        float uniqueTime = ((float)nowTick) * 0.1f + posOffset;

        swing.tiltX = (float) Math.sin(uniqueTime) * 8f;
        swing.tiltZ = (float) Math.cos(uniqueTime * 0.8f) * 6f;
        swing.spin = (float) Math.sin(uniqueTime * 1.5f) * 4f;

        return true;
    }
}
