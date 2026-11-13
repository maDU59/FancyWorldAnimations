package fr.madu59.fwa;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.mixin.client.SetSectionDirtyInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.ChunkAccess;

public class Animations{

    private final ConcurrentHashMap<Long, Bucket> perSectionMap = new ConcurrentHashMap<>();

    public static long sectionKey(BlockPos pos) {
        return SectionPos.asLong(pos);
    }

    public static long sectionKey(int sectionX, int sectionY, int sectionZ) {
        return SectionPos.asLong(sectionX, sectionY, sectionZ);
    }

    public boolean hasSection(BlockPos blockPos) {
        return perSectionMap.containsKey(sectionKey(blockPos));
    }
    
    public void removeAt(BlockPos blockPos) {
        Bucket bucket = this.perSectionMap.get(sectionKey(blockPos));
        if (bucket != null) {
            bucket.remove(blockPos);
            if (bucket.isEmpty()) this.perSectionMap.remove(sectionKey(blockPos), bucket);
        }
    }

    public void removeAt(SectionPos sectionPos) {
        this.perSectionMap.remove(sectionPos.asLong());
    }

    public void removeChunk(ChunkAccess chunk) {
        int minY = chunk.getMinSectionY();
        for (int i = 0; i < chunk.getSectionsCount(); i++) {
            this.perSectionMap.remove(SectionPos.asLong(chunk.getPos().x(), minY + i, chunk.getPos().z()));
        }
    }

    public void removeSafeAt(BlockPos blockPos) {
        Animation animation = getAt(blockPos);
        if(animation == null) return;
        LevelExtractor levelExtractor = Minecraft.getInstance().levelExtractor;
        animation.markForRemoval();
        ((SetSectionDirtyInvoker) levelExtractor).fwa$setSectionDirty(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4, true);
    }

    public boolean containsAt(BlockPos blockPos) {
        Bucket bucket = this.perSectionMap.get(sectionKey(blockPos));
        return bucket != null && bucket.containsKey(blockPos);
    }

    public Animation getAt(BlockPos blockPos) {
        Bucket bucket = this.perSectionMap.get(sectionKey(blockPos));
        return bucket == null ? null : bucket.get(blockPos);
    }

    public void add(BlockPos pos, Animation anim) {
        perSectionMap.computeIfAbsent(sectionKey(pos), key -> new Bucket()).put(pos, anim);
    }

    public boolean isEmpty(){
        return perSectionMap.isEmpty();
    }

    public void clear(){
        perSectionMap.clear();
    }

    public Set<Map.Entry<Long, Bucket>> entrySet(){
        return perSectionMap.entrySet();
    }

    public Collection<Bucket> values(){
        return perSectionMap.values();
    }

    public void clearModelCache(){
        for (Bucket bucket : this.perSectionMap.values()) {
            for (Animation animation : bucket.values()) {
                animation.clearModelCache();
            }
        }
    }

    public static class Bucket {
        private ConcurrentHashMap<BlockPos, Animation> animations = new ConcurrentHashMap<>();
        private volatile boolean isDirty = true;
        private volatile Animation[] flattenedAnimations = null;

        public Animation get(BlockPos pos) {
            return this.animations.get(pos);
        }

        public Collection<Animation> values() {
            return this.animations.values();
        }

        public void remove(BlockPos pos) {
            this.animations.remove(pos);
            this.isDirty = true;
        }

        public void put(BlockPos pos, Animation animation) {
            this.animations.put(pos, animation);
            this.isDirty = true;
        }

        public boolean isEmpty() {
            return this.animations.isEmpty();
        }

        public boolean containsKey(BlockPos pos) {
            return this.animations.containsKey(pos);
        }

        public Animation[] getAnimations() {
            if (this.flattenedAnimations == null || this.isDirty) {
                isDirty = false;
                this.flattenedAnimations = this.animations.values().toArray(new Animation[0]);
            }
            return this.flattenedAnimations;
        }
    }
}
