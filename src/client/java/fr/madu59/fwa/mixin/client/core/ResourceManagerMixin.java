package fr.madu59.fwa.mixin.client.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.madu59.fwa.compat.ModCompat;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

@Mixin(ReloadableResourceManager.class)
public abstract class ResourceManagerMixin {
    @Inject(method = "createReload", at = @At("RETURN"))
    public void fwa$reload(CallbackInfoReturnable<ReloadInstance> cir){
        ModCompat.reload();
    }
}
