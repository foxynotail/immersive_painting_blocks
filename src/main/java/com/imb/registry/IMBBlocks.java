package com.imb.registry;

import com.imb.IMBMod;
import com.imb.blocks.GraffitiBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IMBBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(IMBMod.MODID);

    public static final DeferredBlock<Block> GRAFFITI_BLOCK = BLOCKS.register("graffiti_block",
            () -> new GraffitiBlock(BlockBehaviour.Properties.of()
                    .strength(0.1F)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(GraffitiBlock.LIGHT_LEVEL))));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}