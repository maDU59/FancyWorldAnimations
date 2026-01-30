package fr.madu59.fwa.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public interface SetSectionDirtyInvoker {
    @Invoker("setSectionDirty")
    void fwa$setSectionDirty(int x, int y, int z, boolean isImportant);
}
