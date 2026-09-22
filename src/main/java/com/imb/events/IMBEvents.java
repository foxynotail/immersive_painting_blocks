package com.imb.events;

import com.imb.IMBMod;
import com.imb.blocks.GraffitiBlock;
import com.imb.blocks.GraffitiBlockEntity;
import com.imb.registry.IMBBlocks;
import com.imb.registry.IMBItems;
import net.conczin.immersive_paintings.entity.ImmersiveGlowGraffitiEntity;
import net.conczin.immersive_paintings.entity.ImmersiveGraffitiEntity;
import net.conczin.immersive_paintings.entity.ImmersivePaintingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = IMBMod.MODID)
public class IMBEvents {

    public static boolean convertGraffitiToBlock(Entity target) {

        Level level = target.level();

        if (target instanceof ImmersiveGraffitiEntity || target instanceof ImmersiveGlowGraffitiEntity) {

            ImmersivePaintingEntity paintingEntity = (ImmersivePaintingEntity) target;

            int width = paintingEntity.getPaintingWidth();
            int height = paintingEntity.getPaintingHeight();
            Direction facing = paintingEntity.getDirection();
            ResourceLocation motive = paintingEntity.getMotive();

            // Use getPos() to get the original anchor block (TileX/Y/Z) instead of the calculated entity center
            BlockPos anchorPos = paintingEntity.getPos();
            float yaw = paintingEntity.getYRot();

            ResourceLocation entityType = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
            boolean isGlowing = entityType.getPath().equals("glow_graffiti");

            // Calculate the shift from the exact anchor block to the entity's visual center
            double shiftX = paintingEntity.getX() - (anchorPos.getX() + 0.5D);
            double shiftY = paintingEntity.getY() - (anchorPos.getY() + 0.5D);
            double shiftZ = paintingEntity.getZ() - (anchorPos.getZ() + 0.5D);

            // Zero out the depth shift so it remains flush against the wall
            switch (facing) {
                case NORTH, SOUTH -> shiftZ = 0;
                case EAST, WEST -> shiftX = 0;
                case UP, DOWN -> shiftY = 0;
            }

            paintingEntity.discard();

            generateGraffitiBlock(level, anchorPos, facing, width, height, motive, isGlowing, yaw, (float) shiftX, (float) shiftY, (float) shiftZ);

            return true;
        }

        return false;
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        Entity target = event.getTarget();

        if (event.getLevel().isClientSide() || hand != InteractionHand.MAIN_HAND) return;

        if (player.getItemInHand(hand).is(IMBItems.GRAFFITI_CONVERTER_ITEM)) {

            if (convertGraffitiToBlock(target)) {
                event.setCanceled(true);
            }
        }
    }

    private static void generateGraffitiBlock(Level level, BlockPos center, Direction facing, int width, int height, ResourceLocation motive, boolean isGlowing, float yaw, float shiftX, float shiftY, float shiftZ) {
        BlockState centerState = IMBBlocks.GRAFFITI_BLOCK.get()
                .defaultBlockState()
                .setValue(GraffitiBlock.FACING, facing)
                .setValue(GraffitiBlock.GLOWING, isGlowing);

        level.setBlockAndUpdate(center, centerState);

        BlockEntity blockEntity = level.getBlockEntity(center);
        if (blockEntity instanceof GraffitiBlockEntity graffitiEntity) {
            graffitiEntity.setGraffitiData(motive, width, height, isGlowing, yaw, shiftX, shiftY, shiftZ);
        }
    }
}