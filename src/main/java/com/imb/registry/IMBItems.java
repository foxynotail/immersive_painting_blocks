package com.imb.registry;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.imb.IMBMod.MODID;

public class IMBItems {

    // Create a Deferred Register to hold Items which will all be registered under the "imb" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);


    public static final DeferredItem<Item> GRAFFITI_CONVERTER_ITEM = ITEMS.registerItem(
            "graffiti_converter_item",
            properties -> new Item(
                    properties.stacksTo(1)
            )
    );


    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
