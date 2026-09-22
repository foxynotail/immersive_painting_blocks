package com.imb.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.imb.IMBMod.MODID;

public class IMBCreativeTab {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "imb" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab with the id "imb:imb_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> IMB_TAB = CREATIVE_MODE_TABS.register(
            "imb_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.imb"))
                    .withTabsBefore(CreativeModeTabs.TOOLS_AND_UTILITIES)
                    .icon(() -> IMBItems.GRAFFITI_CONVERTER_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
                        output.accept(IMBItems.GRAFFITI_CONVERTER_ITEM.get());
                    }).build());

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}
