package com.imb.client;

import com.imb.blocks.GraffitiBlock;
import com.imb.blocks.GraffitiBlockEntity;
import com.imb.config.IMBConfigClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class GraffitiBlockEntityRenderer implements BlockEntityRenderer<GraffitiBlockEntity> {

    public GraffitiBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GraffitiBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ResourceLocation motive = blockEntity.getMotive();
        if (motive == null) return;

        ResourceLocation textureLoc = net.conczin.immersive_paintings.ClientPaintingManager.getImageIdentifier(
                motive,
                net.conczin.immersive_paintings.Painting.Size.FULL
        );

        if (textureLoc == null) {
            textureLoc = net.conczin.immersive_paintings.Painting.DEFAULT_IDENTIFIER;
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(textureLoc));

        Direction facing = blockEntity.getBlockState().getValue(GraffitiBlock.FACING);
        int width = blockEntity.getPaintingWidth();
        int height = blockEntity.getPaintingHeight();
        int thickness = blockEntity.getThickness();
        boolean isGlowing = blockEntity.isGlowing();
        float yaw = blockEntity.getYaw();

        int light = isGlowing ? LightTexture.FULL_BRIGHT : packedLight;

        poseStack.pushPose();

        // 1. Move to dead center of the block, plus the even-grid alignment shift
        poseStack.translate(0.5D + blockEntity.getShiftX(), 0.5D + blockEntity.getShiftY(), 0.5D + blockEntity.getShiftZ());

        // 2. Rotate so the local Z+ axis points OUTWARD from the wall
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case UP -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-yaw));
            }
            case DOWN -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(yaw));
            }
        }

        // 3. Push it back so it sits almost flush against the supporting wall
        poseStack.translate(0.0D, 0.0D, -0.5D + 0.00625D);

        float minX = -(width) / 2.0F;
        float maxX = (width) / 2.0F;
        float minY = -(height) / 2.0F;
        float maxY = (height) / 2.0F;

        for (int i = 0; i < thickness; i++) {
            poseStack.pushPose();

            poseStack.translate(0.0D, 0.0D, i * GraffitiBlockEntity.thicknessGap);
            Matrix4f pose = poseStack.last().pose();

            consumer.addVertex(pose, minX, minY, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 1.0F).setOverlay(packedOverlay).setLight(light).setNormal(0, 0, 1);
            consumer.addVertex(pose, maxX, minY, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 1.0F).setOverlay(packedOverlay).setLight(light).setNormal(0, 0, 1);
            consumer.addVertex(pose, maxX, maxY, 0.0F).setColor(255, 255, 255, 255).setUv(1.0F, 0.0F).setOverlay(packedOverlay).setLight(light).setNormal(0, 0, 1);
            consumer.addVertex(pose, minX, maxY, 0.0F).setColor(255, 255, 255, 255).setUv(0.0F, 0.0F).setOverlay(packedOverlay).setLight(light).setNormal(0, 0, 1);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(GraffitiBlockEntity blockEntity) {
        // Dynamically inflate the visual bounding box based on the painting's size.
        // This stops the image from vanishing when you look at the edge of a large painting.
        double radius = Math.max(blockEntity.getPaintingWidth(), blockEntity.getPaintingHeight()) / 2.0D;
        return new AABB(blockEntity.getBlockPos()).inflate(radius + 1.0D);
    }

    @Override
    public int getViewDistance() {
        // Increase the render distance from the default 64 blocks.
        // 256 matches the standard maximum entity tracking distance.
        return IMBConfigClient.RENDER_DISTANCE.get();
    }
}