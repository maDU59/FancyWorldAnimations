package fr.madu59.fwa.mixin;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.model.geom.ModelPart;

@Mixin(ModelPart.class)
public interface ModelPartAccessor {
	@Invoker("addAllChildren")
	void fwa$callForEachChild(BiConsumer<String, ModelPart> partBiConsumer);
}
