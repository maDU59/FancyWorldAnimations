package fr.madu59.fwa.api.animations.impl;

import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDriver;
import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultSwingDriver implements SwingDriver{
    float effects = 0;
    double lastNowTick = 0;

    public boolean getSwing(BlockPos position, BlockState state, double nowTick, HangingSwing swing){
        float speed = 0.1f * SettingsManager.SWINGING_SPEED.getValue();
        if(nowTick != lastNowTick) {
            this.updateWeatherEffect();
            lastNowTick = nowTick;
        }
        float posOffset = (position.getX() * 0.6f) + (position.getZ() * 0.6f);
        float uniqueTime = ((float)nowTick + effects) * speed + posOffset;

        float intensity = SettingsManager.SWINGING_INTENSITY.getValue();

        swing.tiltX = (float) Math.sin(uniqueTime) * 8f * intensity;
        swing.tiltZ = (float) Math.cos(uniqueTime * 0.8f) * 6f * intensity;
        swing.spin = (float) Math.sin(uniqueTime * 1.5f) * 4f * intensity;

        return true;
    }

    private void updateWeatherEffect(){
        Level level = Minecraft.getInstance().level;
        if(level != null && SettingsManager.SWINGING_WEATHER_STATE.getValue()){
            float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
            float frameTime = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
            effects += level.getRainLevel(partialTick) * frameTime;
            effects += level.getThunderLevel(partialTick) * frameTime;
        }
    }
}
