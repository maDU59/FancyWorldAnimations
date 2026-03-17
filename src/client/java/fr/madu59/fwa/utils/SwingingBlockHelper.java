package fr.madu59.fwa.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SwingingBlockHelper {
    public static boolean isSwingingBlock(BlockState blockState){
        return isVerticalChain(blockState) || isHangingLantern(blockState);
    }

    public static boolean isVerticalChain(BlockState blockState){
        return blockState.getBlock() instanceof ChainBlock && blockState.getValue(ChainBlock.AXIS) == Direction.Axis.Y;
    }

    public static boolean isHangingLantern(BlockState blockState){
        return blockState.getBlock() instanceof LanternBlock && blockState.getValue(LanternBlock.HANGING);
    }

    public static int getChainCount(BlockPos blockPos){
        int count = 1;
        MutableBlockPos pos = blockPos.mutable().move(0, 1, 0);
        ClientLevel level = Minecraft.getInstance().level;
        while(isVerticalChain(level.getBlockState(pos))){
            count += 1;
            pos.move(0, 1, 0);
        }
        return count;
    }

    public static boolean isLast(BlockPos blockPos){
        return !isSwingingBlock(Minecraft.getInstance().level.getBlockState(blockPos.below()));
    }
}
