package fr.madu59.fwa.mixin.client.sable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.madu59.fwa.compat.ModCompat;

@Mixin(targets = "dev/ryanhcode/sable/sublevel/render/dispatcher/SubLevelRenderDispatcher", remap = false)
public interface SubLevelRenderDispatcherMixin {

    @Inject(
        method = "createRenderData",
        at = @At("HEAD"), 
        remap = false
    )
    default void onCreateRenderDataStart(Object subLevel, CallbackInfoReturnable<Object> ci) {
        ModCompat.disableAnimationsTemporaryly();
    }

    @Inject(
        method = "createRenderData",
        at = @At("RETURN"), 
        remap = false
    )
    default void onCreateRenderDataEnd(Object subLevel, CallbackInfoReturnable<Object> ci) {
        ModCompat.endAnimationsDisabled();
    }
}
