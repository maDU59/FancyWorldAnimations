package fr.madu59.fwa.compat;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class BlacklistReloadListener implements SimpleSynchronousResourceReloadListener{

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Blacklist.load();
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("fwa","blacklist-loader");
    }
}
