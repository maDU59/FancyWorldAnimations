package fr.madu59.fwa.compat.blacklist;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class BlacklistReloadListener implements PreparableReloadListener{

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.runAsync(() -> {
                    Blacklist.load();
                    FancyWorldAnimationsClient.animations.clearModelCache();
                }, backgroundExecutor)
                .thenCompose(barrier::wait);
    }
}
