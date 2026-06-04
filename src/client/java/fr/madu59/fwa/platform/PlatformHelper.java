package fr.madu59.fwa.platform;

import net.fabricmc.loader.api.FabricLoader;

public class PlatformHelper {

    public static String getPlatformName(){
        return "Fabric";
    }
    
    public static boolean isModLoaded(String modId){
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
