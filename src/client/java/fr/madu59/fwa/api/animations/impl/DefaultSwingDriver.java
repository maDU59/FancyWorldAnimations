package fr.madu59.fwa.api.animations.impl;

import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDriver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultSwingDriver implements SwingDriver{
    float effects = 0;

    public boolean getSwing(BlockPos position, BlockState state, double nowTick, HangingSwing swing){
        float speed = 0.1f;
        ClientLevel level = Minecraft.getInstance().level;
        if(level != null){
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            effects += level.getRainLevel(partialTick) * 0.0005;
            effects += level.getThunderLevel(partialTick) * 0.0008;
        }
        float posOffset = (position.getX() * 0.6f) + (position.getZ() * 0.6f);
        float uniqueTime = ((float)nowTick + effects) * speed + posOffset;

        swing.tiltX = (float) Math.sin(uniqueTime) * 8f;
        swing.tiltZ = (float) Math.cos(uniqueTime * 0.8f) * 6f;
        swing.spin = (float) Math.sin(uniqueTime * 1.5f) * 4f;

        return true;
    }
}
