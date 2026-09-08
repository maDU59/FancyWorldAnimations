package fr.madu59.fwa.api.animations;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.BlockPos;

public class SwingDrivers {

    private static SwingDriver driver = null;

    public static void register(SwingDriver swingDriver){
        driver = swingDriver;
    }

    public static void unregister(){
        driver = null;
    }

    public static SwingDriver getDriver(){
        return driver;
    }

    @ApiStatus.Internal
    public static boolean getSwing(BlockPos position, double nowTick, HangingSwing swing){
        if(driver == null) return false;
        swing.set(0f, 0f, 0f);
        return driver.getSwing(position, nowTick, swing);
    }
}
