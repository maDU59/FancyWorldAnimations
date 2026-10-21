package fr.madu59.fwa.anims;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ChainAnimation extends AbstractChainAnimation {
    
    public ChainAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.LANTERN_STATE.getValue() || SettingsManager.CHAIN_STATE.getValue();
    }

    public boolean hasInfiniteAnimation(){
        return SettingsManager.LANTERN_STATE.getValue() || SettingsManager.CHAIN_STATE.getValue();
    }

    @Override
    public void updateChainCount(){
        ClientLevel level = Minecraft.getInstance().level;
        if(isLast == null && !level.getBlockState(position).isAir()){
            isLast = SwingingBlockHelper.isLast(position);
            if (SettingsManager.CHAIN_GROUNDED.getValue() && isLast && !level.getBlockState(position.below()).isAir()) FancyWorldAnimationsClient.onBlockUpdate(position, defaultState, defaultState);
        }
        chainCount = SwingingBlockHelper.getChainCount(position);
    }

    public void tick(double nowTick) {
        if(isLast == null){
            update();
            return;
        }
        if (!isLast) return;
        if (needUpdate) update();
    }
}
