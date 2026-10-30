package fr.madu59.fwa.rendering;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.model.BakedQuad;

public record AnimationQuad(PoseStack.Pose pose, BakedQuad bakedQuad, int light, int color, AnimationBlockData blockData) {}
