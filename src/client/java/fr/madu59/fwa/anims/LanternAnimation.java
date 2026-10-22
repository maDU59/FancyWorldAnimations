package fr.madu59.fwa.anims;


import fr.madu59.fwa.compat.ModCompat;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

public class LanternAnimation extends AbstractChainAnimation {
    
    public LanternAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
    }

    public boolean hasInfiniteAnimation(){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlockEntity() {
        return !ModCompat.WW_DISPLAY_LANTERNS.equals(BuiltInRegistries.BLOCK.getKey(defaultState.getBlock()));
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override 
    public boolean isLast(){
        return true;
    }

    @Override
    protected void updateChainCount(){
        if(!SettingsManager.CHAIN_STATE.getValue() && !SettingsManager.LANTERN_OVERRIDE.getValue()) chainCount = 1;
        else chainCount = SwingingBlockHelper.getChainCount(position);
    }
}
