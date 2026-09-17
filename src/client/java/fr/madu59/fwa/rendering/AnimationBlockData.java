package fr.madu59.fwa.rendering;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class AnimationBlockData {
    private final BlockState state;
    private final int x;
    private final int y;
    private final int z;
    private final int light;
    
    public AnimationBlockData(BlockState state, BlockPos pos, int light){
        this.state = state;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.light = light;
    }

    public int getPosX(){
        return  x;
    }

    public int getPosY(){
        return  y;
    }

    public int getPosZ(){
        return  z;
    }

    public BlockState getBlockState(){
        return state;
    }

    public int getLight(){
        return light;
    }
}
