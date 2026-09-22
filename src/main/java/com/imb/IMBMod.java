package com.imb;

import com.imb.config.IMBConfigClient;
import com.imb.registry.IMBBlockEntities;
import com.imb.registry.IMBBlocks;
import com.imb.registry.IMBCreativeTab;
import com.imb.registry.IMBItems;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(IMBMod.MODID)
public class IMBMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "imb";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public IMBMod(IEventBus modEventBus, ModContainer modContainer) {

        // Register Config
        //modContainer.registerConfig(ModConfig.Type.SERVER, IMBServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, IMBConfigClient.SPEC);

        // Register the Deferred Register to the mod event bus so blocks get registered
        IMBBlocks.register(modEventBus);

        // Register the Deferred Register to the mod event bus so items get registered
        IMBItems.register(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        IMBCreativeTab.register(modEventBus);

        // Register Entities
        //IMBEntities.register(modEventBus);
        //modEventBus.addListener(IMBEntities::registerAttributes);

        // Register Block Entities
        IMBBlockEntities.register(modEventBus);

        // Register Sounds
        //IMBSounds.register(modEventBus);

        // Register Particles
        //IMBParticles.register(modEventBus);

        // Tells the game to run your commonSetup method during the mod setup phase
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (IMBMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        //LOGGER.info("HELLO from server starting");
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }

        // Register the renderer to the entity
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(IMBBlockEntities.GRAFFITI_BLOCK_ENTITY.get(), com.imb.client.GraffitiBlockEntityRenderer::new);
        }

        // Register the model layer we created
        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            //event.registerLayerDefinition(SomethingModel.LAYER_LOCATION, SomethingModel::createBodyLayer);
        }

        // Register Particles
        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
            //event.registerSpriteSet(IMBParticles.SOME_PARTICLE.get(), SomeParticle.Provider::new);
        }


    }
}
