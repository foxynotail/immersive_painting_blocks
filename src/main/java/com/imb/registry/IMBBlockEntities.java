package com.imb.registry;

import com.imb.IMBMod;
import com.imb.blocks.GraffitiBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class IMBBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IMBMod.MODID);

    public static final Supplier<BlockEntityType<GraffitiBlockEntity>> GRAFFITI_BLOCK_ENTITY = BLOCK_ENTITIES.register("graffiti_block_entity",
            () -> BlockEntityType.Builder.of(GraffitiBlockEntity::new, IMBBlocks.GRAFFITI_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}