package fr.madu59.fwa.compat.endremastered;

import com.teamremastered.endrem.blocks.AncientPortalFrame;
import com.teamremastered.endrem.blocks.ERFrameProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class EndRemasteredCompat{

    public static boolean isEndRemasteredPortal(BlockState state){
        if(state == null) return false;
        return state.getBlock() instanceof AncientPortalFrame;
    }

    public static boolean isEmpty(BlockState state){
        if(state == null) return true;
        return state.getValue(AncientPortalFrame.EYE) == ERFrameProperties.EMPTY;
    }

    public static BlockState setEmpty(BlockState state){
        if(state == null) return null;
        return state.setValue(AncientPortalFrame.EYE, ERFrameProperties.EMPTY);
    }
}
