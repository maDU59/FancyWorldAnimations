// package fr.madu59.fwa.mixin.client;

// import net.minecraft.client.renderer.block.ModelBlockRenderer;
// import net.minecraft.core.BlockPos;
// import net.minecraft.world.level.BlockAndTintGetter;
// import net.minecraft.world.level.block.state.BlockState;

// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.injection.At;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// import com.mojang.blaze3d.vertex.PoseStack;
// import com.mojang.blaze3d.vertex.VertexConsumer;

// import fr.madu59.fwa.FancyWorldAnimationsClient;

// import java.util.List;

// @Mixin(ModelBlockRenderer.class)
// public class ModelBlockRendererMixin {
    
//     @Inject(
//         method = "tesselateBlock",
//         at = @At("HEAD"),
//         cancellable = true
//     )
//     private void fwa$tesselateBlock(
//         BlockAndTintGetter world,
//         List<?> parts,
//         BlockState state,
//         BlockPos pos,
//         PoseStack matrices,
//         VertexConsumer vertexConsumer,
//         boolean cull,
//         int overlay,
//         CallbackInfo ci
//     ) {
//         if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
//             ci.cancel();
//         }
//     }
// }

// Does the same as RenderSectionRegionMixin