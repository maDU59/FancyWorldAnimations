package fr.madu59.fwa.mixin.client.blockentity.renderer.sign;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.api.animations.SwingDrivers;
import fr.madu59.fwa.rendering.blockentity.sign.HangingSignRenderStateExt;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HangingSignRenderer.class)
public abstract class HangingSignRendererMixin {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    public void fwa$extractRenderState(SignBlockEntity blockEntity, HangingSignRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress, CallbackInfo ci) {
        if (state instanceof HangingSignRenderStateExt ext) {
            if(blockEntity.getBlockState().getBlock() instanceof CeilingHangingSignBlock){
                HangingSwing fwa$swing = new HangingSwing();
                if(SwingDrivers.getSwing(blockEntity.getBlockPos(), blockEntity.getBlockState(), FancyWorldAnimationsClient.getPartialTick(), fwa$swing)){
                    ext.fwa$setSwing(fwa$swing);
                    ext.fwa$setDirection(blockEntity.getBlockState().getValue(CeilingHangingSignBlock.ROTATION));
                    return;
                }
            }
            ext.fwa$setSwing(null);
            ext.fwa$setDirection(-1);
        }
    }
}
