package fr.madu59.fwa.rendering;

import java.util.Arrays;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.resources.model.geometry.BakedQuad;

public class QuadBatch {
    private PoseStack.Pose[] poses;
    private BakedQuad[] bakedQuads;
    private int[] lights;
    private int[] colors;
    private AnimationBlockData[] blockData;
    private int size;

    public QuadBatch() {
        this(32);
    }

    public QuadBatch(int capacity) {
        capacity = Math.max(1, capacity);
        this.poses = new PoseStack.Pose[capacity];
        this.bakedQuads = new BakedQuad[capacity];
        this.lights = new int[capacity];
        this.colors = new int[capacity];
        this.blockData = new AnimationBlockData[capacity];
        this.size = 0;
    }

    public void add(PoseStack.Pose pose, BakedQuad bakedQuad, int light, int color, AnimationBlockData data) {
        ensureCapacity(size + 1);
        poses[size] = pose;
        bakedQuads[size] = bakedQuad;
        lights[size] = light;
        colors[size] = color;
        blockData[size] = data;
        size++;
    }

    public int size() {
        return size;
    }

    public PoseStack.Pose pose(int index) {
        return poses[index];
    }

    public BakedQuad bakedQuad(int index) {
        return bakedQuads[index];
    }

    public int light(int index) {
        return lights[index];
    }

    public int color(int index) {
        return colors[index];
    }

    public AnimationBlockData blockData(int index) {
        return blockData[index];
    }

    private void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity <= poses.length) return;

        poses = Arrays.copyOf(poses, size << 1);
        bakedQuads = Arrays.copyOf(bakedQuads, size << 1);
        lights = Arrays.copyOf(lights, size << 1);
        colors = Arrays.copyOf(colors, size << 1);
        blockData = Arrays.copyOf(blockData, size << 1);
    }

    public void clear() {
        Arrays.fill(poses, 0, size, null);
        Arrays.fill(bakedQuads, 0, size, null);
        Arrays.fill(blockData, 0, size, null);
        size = 0;
    }
}
