package fr.madu59.fwa.mixin.client.lanterns;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import fr.madu59.fwa.blockentity.LanternBlockEntity;
import fr.madu59.fwa.blockentity.registry.BlockEntityTypes;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.LanternBlockInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin extends Block implements EntityBlock, LanternBlockInterface{

    @Shadow
    private static VoxelShape SHAPE_HANGING;

    public LanternBlockMixin(BlockBehaviour.Properties properties){
        super(properties);
    }

    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState){
        if (blockState.getValue(LanternBlock.HANGING) && SettingsManager.LANTERN_STATE.getValue()){
            return new LanternBlockEntity(blockPos, blockState);
        }
        else{
            return null;
        }
    }

    protected RenderShape getRenderShape(BlockState blockState) {
        return blockState.getValue(LanternBlock.HANGING) && BlockEntityTypes.LANTERN.isValid(blockState) && SettingsManager.LANTERN_STATE.getValue() ? RenderShape.INVISIBLE : super.getRenderShape(blockState);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    public VoxelShape fwa$getShape(){
        return SHAPE_HANGING;
    }
}
