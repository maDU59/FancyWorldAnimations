package fr.madu59.fwa.mixin;

import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.fwa.utils.FwaModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

@Mixin(Model.class)
abstract class ModelMixin<S> implements FwaModel<S>{
	@Shadow
	public abstract ModelPart root();

	@Unique
	private final Map<String, ModelPart> childPartMap = new Object2ObjectOpenHashMap<>();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fillChildPartMap(ModelPart root, Function<Identifier, RenderType> layerFactory, CallbackInfo ci) {
		((ModelPartAccessor) (Object) root).fwa$callForEachChild(childPartMap::putIfAbsent);
	}

	@Nullable
	public ModelPart getChildPart(String name) {
		return childPartMap.get(name);
	}
}