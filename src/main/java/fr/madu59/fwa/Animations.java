package fr.madu59.fwa;

import java.util.concurrent.ConcurrentHashMap;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.mixin.SetSectionDirtyInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.ChunkAccess;import net.minecraft.world.level.chunk.LevelChunk;

public class Animations{

    public final ConcurrentHashMap<Long, ConcurrentHashMap<BlockPos, Animation>> animations = new ConcurrentHashMap<>();

    public static long sectionKey(BlockPos pos) {
        return SectionPos.asLong(pos);
    }

    public static long sectionKey(int sectionX, int sectionY, int sectionZ) {
        return SectionPos.asLong(sectionX, sectionY, sectionZ);
    }

    public boolean hasSection(BlockPos blockPos) {
        return animations.contains(sectionKey(blockPos));
    }
    
    public void removeAt(BlockPos blockPos) {
        ConcurrentHashMap<BlockPos, Animation> chunkAnimations = this.animations.get(sectionKey(blockPos));
        if (chunkAnimations != null) {
            chunkAnimations.remove(blockPos);
            if (chunkAnimations.isEmpty()) this.animations.remove(sectionKey(blockPos), chunkAnimations);
        }
    }

    public void removeAt(SectionPos sectionPos) {
        this.animations.remove(sectionPos.asLong());
    }

    public void removeChunk(ChunkAccess chunk) {
        int minY = chunk.getMinSection();
        for (int i = 0; i < chunk.getSectionsCount(); i++) {
            this.animations.remove(SectionPos.asLong(chunk.getPos().x, minY + i, chunk.getPos().z));
        }
    }

    public void removeSafeAt(BlockPos blockPos) {
        Animation animation = getAt(blockPos);
        if(animation == null) return;
        LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
        animation.markForRemoval();
        ((SetSectionDirtyInvoker) levelRenderer).fwa$setSectionDirty(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4, true);
    }

    public boolean containsAt(BlockPos blockPos) {
        ConcurrentHashMap<BlockPos, Animation> chunkAnimations = this.animations.get(sectionKey(blockPos));
        return chunkAnimations != null && chunkAnimations.containsKey(blockPos);
    }

    public Animation getAt(BlockPos blockPos) {
        ConcurrentHashMap<BlockPos, Animation> chunkAnimations = this.animations.get(sectionKey(blockPos));
        return chunkAnimations == null ? null : chunkAnimations.get(blockPos);
    }

    public void add(BlockPos pos, Animation anim) {
        animations.computeIfAbsent(sectionKey(pos), key -> new ConcurrentHashMap<>()).put(pos, anim);
    }

    public boolean isEmpty(){
        return animations.isEmpty();
    }

    public void clearModelCache(){
        for (ConcurrentHashMap<BlockPos, Animation> chunkAnimations : this.animations.values()) {
            for (Animation animation : chunkAnimations.values()) {
                animation.clearModelCache();
            }
        }
    }
}