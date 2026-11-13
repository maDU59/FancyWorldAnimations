package fr.madu59.fwa.compat.endremastered;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamremastered.endrem.block.AncientPortalFrame;
import com.teamremastered.endrem.block.AncientPortalFrameEntity;
import com.teamremastered.endrem.client.AncientPortalRenderer;
import com.teamremastered.endrem.client.AncientPortalState;

import fr.madu59.fwa.rendering.AnimationRenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class EndRemasteredCompat{

    public static boolean isEndRemasteredPortal(BlockState state){
        return state.getBlock() instanceof AncientPortalFrame;
    }

    public static <S extends BlockEntityRenderState> boolean renderEndPortalFrameAnimation(AnimationRenderingContext context, PoseStack poseStack, BlockPos position, int light){

        Level level = Minecraft.getInstance().level;
        if (level == null || !(level.getBlockEntity(position) instanceof AncientPortalFrameEntity portal)) return false;

        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        BlockEntityRenderer<AncientPortalFrameEntity, S> rendererObject = dispatcher.getRenderer(portal);

        if (!(rendererObject instanceof AncientPortalRenderer renderer))  return false;

        AncientPortalState portalState = renderer.createRenderState();
        renderer.extractRenderState(portal, portalState, 0, context.getCameraPos(), new CrumblingOverlay(0, poseStack.last()));
        renderer.submit(portalState, poseStack, context.getSubmitNodeCollector(), context.getCameraRenderState());
        return true;
    }
}
