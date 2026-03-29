package fr.madu59.fwa.compat;

import fr.madu59.fwa.FancyWorldAnimationsClient.Type;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ModCompat {

    private final static String DRAMATIC_DOORS_NAMESPACE = "dramaticdoors";
    private final static boolean IS_AMENDMENTS_LOADED = FabricLoader.getInstance().isModLoaded("amendments");
    private final static boolean IS_IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");
    
    public static Type typeOf(Block block){
        if(DRAMATIC_DOORS_NAMESPACE.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())) return Type.DOOR;
        return Type.USELESS;
    }

    public static boolean isOpen(BlockState state, Block block){
        if (DRAMATIC_DOORS_NAMESPACE.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace())) return state.getValue(BlockStateProperties.OPEN);
        return false;
    }

    public static boolean isAmendmentsLoaded(){
        return IS_AMENDMENTS_LOADED;
    }

    public static boolean isIrisLoaded(){
        return IS_IRIS_LOADED;
    }

}
