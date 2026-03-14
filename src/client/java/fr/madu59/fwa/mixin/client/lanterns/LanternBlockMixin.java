package fr.madu59.fwa.mixin.client.lanterns;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import fr.madu59.fwa.utils.LanternBlockInterface;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

@Mixin(LanternBlock.class)
public abstract class LanternBlockMixin extends Block implements LanternBlockInterface{

    @Shadow
    private static VoxelShape HANGING_AABB;

    public LanternBlockMixin(BlockBehaviour.Properties properties){
        super(properties);
    }

    public VoxelShape fwa$getShape(){
        return HANGING_AABB;
    }
}
