package fr.madu59.fwa.compat;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import fr.madu59.fwa.platform.PlatformHelper;

public class ModCompatPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains(".sodium.")) {
            return PlatformHelper.isModLoaded("sodium") || PlatformHelper.isModLoaded("embeddium");
        }
        if (mixinClassName.contains(".iris.")) {
            return PlatformHelper.isModLoaded("iris") || PlatformHelper.isModLoaded("oculus");
        }
        if (mixinClassName.contains(".scholar.")) {
            return PlatformHelper.isModLoaded("scholar");
        }
        if (mixinClassName.contains(".entityculling.")) {
            return PlatformHelper.isModLoaded("entityculling");
        }
        if (mixinClassName.contains(".vulkanmod.")) {
            return PlatformHelper.isModLoaded("vulkanmod");
        }
        return true;
    }

    // Leave these empty/default
    @Override public void onLoad(String mixinPackage) {}
}