package fr.madu59.fwa.compat.endremastered;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamremastered.endrem.block.AncientPortalFrame;
import com.teamremastered.endrem.block.AncientPortalFrameEntity;
import com.teamremastered.endrem.client.AncientPortalRenderer;

import fr.madu59.fwa.rendering.AnimationRenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class EndRemasteredCompat{

    public static boolean isEndRemasteredPortal(BlockState state){
        return state.getBlock() instanceof AncientPortalFrame;
    }

    public static boolean renderEndPortalFrameAnimation(AnimationRenderingContext context, PoseStack poseStack, BlockPos position, int light){

        Level level = Minecraft.getInstance().level;
        if (level == null || !(level.getBlockEntity(position) instanceof AncientPortalFrameEntity portal)) return false;

        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        BlockEntityRenderer<AncientPortalFrameEntity> rendererObject = dispatcher.getRenderer(portal);

        if (!(rendererObject instanceof AncientPortalRenderer renderer))  return false;

        renderer.render(portal, (float)context.getNowTick(), poseStack, context.getBufferSource(), light, OverlayTexture.NO_OVERLAY, new Vec3(0, 0, 0));
        return true;
    }
}
