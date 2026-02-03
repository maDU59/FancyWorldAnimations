package fr.madu59.fwa.utils;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.Model;
import org.jspecify.annotations.Nullable;

public interface FwaModel<S> {
    @Nullable
    ModelPart getChildPart(String name);
    
    void copyTransforms(Model<?> model);
}