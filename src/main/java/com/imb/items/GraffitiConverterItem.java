package com.imb.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GraffitiConverterItem extends Item {

    public GraffitiConverterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);

        if (customData != null) {
            CompoundTag tag = customData.copyTag();

            tooltipComponents.add(
                    Component.literal("Use on Immersive Paintings Graffiti to set in place as a block")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );

            tooltipComponents.add(
                    Component.literal("Use on Converted Graffiti Blocks to increase thickness")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
        }

        super.appendHoverText(itemStack, context, tooltipComponents, tooltipFlag);
    }
}
