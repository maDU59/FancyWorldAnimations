package fr.madu59.fwa.api.animations;

import org.jetbrains.annotations.ApiStatus;

import fr.madu59.fwa.api.animations.impl.DefaultSwingDriver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SwingDrivers {
    private static SwingDriver DEFAULT_DRIVER = new DefaultSwingDriver();
    private static SwingDriver driver = DEFAULT_DRIVER;

    public static void register(SwingDriver swingDriver){
        driver = swingDriver;
    }

    public static void unregister(){
        driver = DEFAULT_DRIVER;
    }

    public static SwingDriver getDriver(){
        return driver;
    }

    @ApiStatus.Internal
    public static boolean getSwing(BlockPos position, BlockState state, double nowTick, HangingSwing swing){
        swing.set(0f, 0f, 0f);
        return driver.getSwing(position, state, nowTick, swing);
    }
}
