package fr.madu59.fwa.compat;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class BlacklistReloadListener implements ResourceManagerReloadListener{

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Blacklist.load();
    }
}
