package fr.madu59.fwa.utils;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.anims.ChainAnimation;
import fr.madu59.fwa.anims.LanternAnimation;
import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class SwingingBlockHelper {
    public static boolean isActiveSwingingBlock(BlockState blockState){
        return isVerticalChain(blockState) || isActiveHangingLantern(blockState);
    }

    public static boolean isSwingingBlock(BlockState blockState){
        return isVerticalChain(blockState) || isHangingLantern(blockState);
    }

    public static boolean isVerticalChain(BlockState blockState){
        return blockState.getBlock() instanceof ChainBlock && blockState.getValue(ChainBlock.AXIS) == Direction.Axis.Y;
    }

    public static boolean isVerticalChain(Animation animation){
        return animation instanceof ChainAnimation;
    }

    public static boolean isHangingLantern(BlockState blockState){
        return FancyWorldAnimationsClient.typeOf(blockState) == FancyWorldAnimationsClient.Type.LANTERN && blockState.getValueOrElse(BlockStateProperties.HANGING, false);
    }

    public static boolean isActiveHangingLantern(BlockState blockState){
        return isHangingLantern(blockState) && SettingsManager.LANTERN_STATE.getValue() && SettingsManager.MOD_TOGGLE.getValue();
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

    public static boolean isLastGrounded(BlockPos blockPos){
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos lastPos = SwingingBlockHelper.getLast(blockPos);
        return !level.getBlockState(lastPos.below()).isAir() && SwingingBlockHelper.isVerticalChain(level.getBlockState(lastPos));
    }

    public static boolean isLast(BlockPos blockPos){
        Animation anim = FancyWorldAnimationsClient.animations.animations.get(blockPos.below());
        return !isSwingingBlock(Minecraft.getInstance().level.getBlockState(blockPos.below())) && !(anim instanceof LanternAnimation || anim instanceof ChainAnimation);
    }

    public static BlockPos getLast(BlockPos blockPos){
        MutableBlockPos pos = blockPos.mutable();
        while (isSwingingBlock(Minecraft.getInstance().level.getBlockState(pos))){
            pos.move(0,-1,0);
        }
        return pos.move(0,1,0);
    }

    public static BlockState getLastAnimation(BlockPos blockPos){
        MutableBlockPos pos = blockPos.mutable();
        Animation anim = FancyWorldAnimationsClient.animations.animations.get(pos);
        if(anim == null) {
            pos.move(0,-1,0);
            anim = FancyWorldAnimationsClient.animations.animations.get(pos);
            if(anim == null) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        while (anim != null){
            pos.move(0,-1,0);
            anim = FancyWorldAnimationsClient.animations.animations.get(pos);
            if (anim != null && isHangingLantern(anim.getDefaultState())) break;
        }
        if(anim == null) {
            pos.move(0,1,0);
            anim = FancyWorldAnimationsClient.animations.animations.get(pos);
        }
        return anim.getDefaultState();
    }
}
