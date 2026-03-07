package fr.madu59.fwa.blockentity;

import fr.madu59.fwa.blockentity.registry.BlockEntityTypes;
import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LanternBlockEntity extends BlockEntity{
    public LanternBlockEntity(BlockPos blockPos, BlockState blockState){
        super(BlockEntityTypes.LANTERN, blockPos, blockState);
    }

    public boolean isHanging(){
        return this.getBlockState().getValue(LanternBlock.HANGING);
    }

    public boolean isEnabled(){
        return SettingsManager.LANTERN_STATE.getValue();
    }
}
