package com.imb.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class IMBConfigClient {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue RENDER_DISTANCE;

    static {
        BUILDER.push("Render Distance");

        RENDER_DISTANCE = BUILDER
                .comment("How far in blocks should the converted Graffiti Blocks continue to render away from the player.")
                .defineInRange("render_distance", 256, 16, 512);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}