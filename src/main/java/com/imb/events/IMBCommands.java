package com.imb.events;

import com.imb.IMBMod;
import com.imb.blocks.GraffitiBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.conczin.immersive_paintings.entity.ImmersiveGlowGraffitiEntity;
import net.conczin.immersive_paintings.entity.ImmersiveGraffitiEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.*;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.List;

import static com.imb.IMBMod.MODID;

@EventBusSubscriber(modid = MODID)

public class IMBCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // Base command: /imb
        dispatcher.register(Commands.literal("imb")
                // Restrict to server operators
                .requires(source -> source.hasPermission(2))
                // Sub-command node: /imb convert
                .then(Commands.literal("convert")
                        .then(Commands.argument("distance", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int distance = IntegerArgumentType.getInteger(context, "distance");
                                    return imbSetGraffitiBlocks(context.getSource(), distance);
                                })
                        )
                )
                .then(Commands.literal("thickness")
                        .then(Commands.argument("thickness", IntegerArgumentType.integer())
                                .then(Commands.argument("distance", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int thickness = IntegerArgumentType.getInteger(context, "thickness");
                                            int distance = IntegerArgumentType.getInteger(context, "distance");
                                            return imbSetGraffitiThickness(context.getSource(), thickness, distance);
                                        })
                                )
                        )
                )

                .then(Commands.literal("stats")
                        .then(Commands.argument("distance", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int distance = IntegerArgumentType.getInteger(context, "distance");
                                    return imbCountEntities(context.getSource(), distance);
                                })
                        )
                )
        );
    }

    private static int imbSetGraffitiBlocks(CommandSourceStack source, int distance) {

        Player player = source.getPlayer();
        if (player == null) return 1;

        Level level = player.level();

        Vec3 pos = player.position();

        double x = pos.x();
        double y = pos.y();
        double z = pos.z();
        double radius = distance; // distance in blocks

        // Create a cubic bounding box (AABB) around the center
        AABB searchBox = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        // Retrieve all entities within the bounding box
        List<ImmersiveGraffitiEntity> graffitiEntities = level.getEntitiesOfClass(ImmersiveGraffitiEntity.class, searchBox);
        List<ImmersiveGlowGraffitiEntity> glowGraffitiEntities = level.getEntitiesOfClass(ImmersiveGlowGraffitiEntity.class, searchBox);

        for (Entity entity : graffitiEntities) {
            // Convert to block
            IMBEvents.convertGraffitiToBlock(entity);
            source.sendSuccess(() -> Component.literal("Converted Graffiti Entity " + entity.getName().getString() + " at " + entity.position() + " to Graffiti Block"), true);
        }

        for (Entity entity : glowGraffitiEntities) {
            // Convert to block
            IMBEvents.convertGraffitiToBlock(entity);
            source.sendSuccess(() -> Component.literal("Converted Graffiti Entity " + entity.getName().getString() + " at " + entity.position() + " to Graffiti Block"), true);
        }
        return 1;
    }

    public static List<GraffitiBlockEntity> getNearbyGraffitiBlockEntities(Level level, BlockPos center, int radius) {
        List<GraffitiBlockEntity> graffitiBlockEntities = new ArrayList<>();

        // Define bounds
        int minX = center.getX() - radius;
        int minY = Math.max(level.getMinBuildHeight(), center.getY() - radius);
        int minZ = center.getZ() - radius;
        int maxX = center.getX() + radius;
        int maxY = Math.min(level.getMaxBuildHeight(), center.getY() + radius);
        int maxZ = center.getZ() + radius;

        // Iterate through positions (efficient only for small radiuses)
        BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ).forEach(pos -> {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GraffitiBlockEntity graffitiBlockEntity) {
                graffitiBlockEntities.add(graffitiBlockEntity);
            }
        });

        return graffitiBlockEntities;
    }

    private static int imbSetGraffitiThickness(CommandSourceStack source, int thickness, int distance) {

        Player player = source.getPlayer();
        if (player == null) return 1;

        Level level = player.level();

        BlockPos blockPos = player.blockPosition();

        List<GraffitiBlockEntity> graffitiBlockEntities = getNearbyGraffitiBlockEntities(level, blockPos, distance);

        for (GraffitiBlockEntity blockEntity : graffitiBlockEntities) {
            blockEntity.setThickness(thickness);
            source.sendSuccess(() -> Component.literal("Set Graffiti Thickness at " + blockPos + " to " + thickness), true);
        }
        return 1;
    }

    private static int imbCountEntities(CommandSourceStack source, int distance) {

        Player player = source.getPlayer();
        IMBMod.LOGGER.info("Run Stats");
        if (player == null) return 1;
        IMBMod.LOGGER.info("Stats Running");

        Level level = player.level();

        IMBMod.LOGGER.info("Stats: Get Nearby Entities");
        List<Entity> entities = getNearbyEntities(level, player.position(), distance);
        IMBMod.LOGGER.info("Stats: Get Nearby Block Entities");
        List<BlockEntity> blockEntities = getNearbyBlockEntities(level, player.blockPosition(), distance);

        IMBMod.LOGGER.info("Stats: Build Scoreboard");
        Scoreboard scoreboard = level.getScoreboard();
        Objective objective = scoreboard.getObjective("imb_stats");
        if (objective == null) {
            objective = scoreboard.addObjective("imb_stats", ObjectiveCriteria.DUMMY, Component.literal("IMB Stats"),
                    ObjectiveCriteria.RenderType.INTEGER, false, null);
        }

        scoreboard.setDisplayObjective(DisplaySlot.SIDEBAR, objective);

        // Entities

        ScoreHolder graffitiHolder = ScoreHolder.forNameOnly("[E] Graffiti");
        ScoreHolder glowGraffitiHolder = ScoreHolder.forNameOnly("[E] Glow Graffiti");
        ScoreHolder superGlueHolder = ScoreHolder.forNameOnly("[E] Super Glue");
        ScoreHolder controlledContraptionHolder = ScoreHolder.forNameOnly("[E] Controlled Contraptions");
        ScoreHolder carriageContraptionHolder = ScoreHolder.forNameOnly("[E] Carriage Contraptions");
        ScoreHolder orientedContraptionHolder = ScoreHolder.forNameOnly("[E] Oriented Contraptions");
        ScoreHolder strawStatueHolder = ScoreHolder.forNameOnly("[E] Straw Statues");
        ScoreHolder itemFrameHolder = ScoreHolder.forNameOnly("[E] Item Frames");
        ScoreHolder armorStandHolder = ScoreHolder.forNameOnly("[E] Armor Stands");
        ScoreHolder otherEntityHolder = ScoreHolder.forNameOnly("[E] Other Entities");
        ScoreHolder totalEntityHolder = ScoreHolder.forNameOnly("[E] Total Entities");

        ScoreHolder graffitiBlockHolder = ScoreHolder.forNameOnly("[BE] Graffiti Blocks");
        ScoreHolder copyCatHolder = ScoreHolder.forNameOnly("[BE] Copycat Blocks");
        ScoreHolder createHolder = ScoreHolder.forNameOnly("[BE] Create Blocks");
        ScoreHolder createContainerHolder = ScoreHolder.forNameOnly("[BE] Create Containers");
        ScoreHolder wallLanternHolder = ScoreHolder.forNameOnly("[BE] Wall Lanterns");
        ScoreHolder chestHolder = ScoreHolder.forNameOnly("[BE] Chests");
        ScoreHolder barrelHolder = ScoreHolder.forNameOnly("[BE] Barrels");
        ScoreHolder sculkHolder = ScoreHolder.forNameOnly("[BE] Sculk Related");
        ScoreHolder otherBlockEntityHolder = ScoreHolder.forNameOnly("[BE] Other Block Entities");
        ScoreHolder totalBlockEntityHolder = ScoreHolder.forNameOnly("[BE] Total Block Entities");

        ScoreHolder totalTotalHolder = ScoreHolder.forNameOnly("Total Entities + Block Entities");

        scoreboard.resetAllPlayerScores(graffitiHolder);
        scoreboard.resetAllPlayerScores(glowGraffitiHolder);
        scoreboard.resetAllPlayerScores(superGlueHolder);
        scoreboard.resetAllPlayerScores(controlledContraptionHolder);
        scoreboard.resetAllPlayerScores(carriageContraptionHolder);
        scoreboard.resetAllPlayerScores(orientedContraptionHolder);
        scoreboard.resetAllPlayerScores(strawStatueHolder);
        scoreboard.resetAllPlayerScores(itemFrameHolder);
        scoreboard.resetAllPlayerScores(armorStandHolder);
        scoreboard.resetAllPlayerScores(otherEntityHolder);
        scoreboard.resetAllPlayerScores(totalEntityHolder);

        scoreboard.resetAllPlayerScores(graffitiBlockHolder);
        scoreboard.resetAllPlayerScores(wallLanternHolder);
        scoreboard.resetAllPlayerScores(chestHolder);
        scoreboard.resetAllPlayerScores(barrelHolder);
        scoreboard.resetAllPlayerScores(sculkHolder);
        scoreboard.resetAllPlayerScores(copyCatHolder);
        scoreboard.resetAllPlayerScores(createHolder);
        scoreboard.resetAllPlayerScores(createContainerHolder);
        scoreboard.resetAllPlayerScores(otherBlockEntityHolder);
        scoreboard.resetAllPlayerScores(totalBlockEntityHolder);

        scoreboard.resetAllPlayerScores(totalTotalHolder);

        IMBMod.LOGGER.info("Stats: Count");

        int graffiti = 0;
        int glowGraffiti = 0;
        int superGlue = 0;
        int controlledContraption = 0;
        int carriageContraption = 0;
        int orientedContraption = 0;
        int strawStatue = 0;
        int itemFrame = 0;
        int armorStand = 0;
        int otherEntity = 0;
        int totalEntityScore = 0;


        for (Entity entity : entities) {

            String entityClass = entity.getClass().getSimpleName();

            if (entity instanceof ImmersiveGraffitiEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(graffitiHolder, objective);
                score.set(score.get() + 1);
                graffiti++;
                totalEntityScore++;
            } else if (entity instanceof ImmersiveGlowGraffitiEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(glowGraffitiHolder, objective);
                score.set(score.get() + 1);
                glowGraffiti++;
                totalEntityScore++;
            } else if (entityClass.contains("SuperGlueEntity")) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(superGlueHolder, objective);
                score.set(score.get() + 1);
                superGlue++;
                totalEntityScore++;
            } else if (entityClass.contains("ControlledContraptionEntity")) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(controlledContraptionHolder, objective);
                score.set(score.get() + 1);
                controlledContraption++;
                totalEntityScore++;
            } else if (entityClass.contains("CarriageContraptionEntity")) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(carriageContraptionHolder, objective);
                score.set(score.get() + 1);
                carriageContraption++;
                totalEntityScore++;
            } else if (entityClass.contains("OrientedContraptionEntity")) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(orientedContraptionHolder, objective);
                score.set(score.get() + 1);
                orientedContraption++;
                totalEntityScore++;
            } else if (entityClass.contains("StrawStatue")) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(strawStatueHolder, objective);
                score.set(score.get() + 1);
                strawStatue++;
                totalEntityScore++;
            } else if (entity instanceof ItemFrame) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(itemFrameHolder, objective);
                score.set(score.get() + 1);
                itemFrame++;
                totalEntityScore++;
            } else if (entity instanceof ArmorStand) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(armorStandHolder, objective);
                score.set(score.get() + 1);
                armorStand++;
                totalEntityScore++;
            } else {
                IMBMod.LOGGER.info("EntityName: " + entityClass);
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(otherEntityHolder, objective);
                score.set(score.get() + 1);
                otherEntity++;
                totalEntityScore++;
            }
        }

        ScoreAccess totalEntityScoreAccess = scoreboard.getOrCreatePlayerScore(totalEntityHolder, objective);
        totalEntityScoreAccess.set(totalEntityScore);
        int totalEScore = graffiti + glowGraffiti + superGlue + controlledContraption + carriageContraption + orientedContraption + strawStatue + itemFrame + armorStand + otherEntity;
        IMBMod.LOGGER.info("Total Entities: " + totalEntityScore + " Check: " + totalEScore);

        // Block Entities

        int graffitiBlock = 0;
        int wallLantern = 0;
        int chest = 0;
        int barrel = 0;
        int sculk = 0;
        int copycat = 0;
        int create = 0;
        int createContainer = 0;
        int otherBlockEntity = 0;
        int totalBlockEntityScore = 0;

        List<String> copycatBE = new ArrayList<>();
        copycatBE.add("CCCopycatBlockEntity");
        copycatBE.add("MultiStateCopycatBlockEntity");
        copycatBE.add("CopycatFluidPipeBlockEntity");
        copycatBE.add("CopycatBlockEntity");
        copycatBE.add("CCCopycatBlockEntity");

        List<String> createBE = new ArrayList<>();
        createBE.add("FakeTrackBlockEntity");
        createBE.add("TrackBlockEntity");
        createBE.add("MechanicalBearingBlockEntity");
        createBE.add("FlywheelBlockEntity");
        createBE.add("KineticBlockEntity");
        createBE.add("BracketedKineticBlockEntity");
        createBE.add("HandCrankBlockEntity");
        createBE.add("SignalBlockEntity");
        createBE.add("CRBogeyBlockEntity");
        createBE.add("FluidPipeBlockEntity");
        createBE.add("LargeWaterWheelBlockEntity");
        createBE.add("WaterWheelBlockEntity");
        createBE.add("PlacardBlockEntity");
        createBE.add("CrushingWheelBlockEntity");
        createBE.add("BeltBlockEntity");
        createBE.add("DieselSmokeStackBlockEntity");
        createBE.add("RedstoneLinkBlockEntity");
        createBE.add("CabinetBlockEntity");
        createBE.add("SequencedGearshiftBlockEntity");
        createBE.add("MechanicalPistonBlockEntity");
        createBE.add("TableClothBlockEntity");
        createBE.add("PackagerBlockEntity");
        createBE.add("FunnelBlockEntity");
        createBE.add("BoilerBlockEntity");
        createBE.add("GearboxBlockEntity");
        createBE.add("EncasedFanBlockEntity");
        createBE.add("SpeedControllerBlockEntity");
        createBE.add("BrassTunnelBlockEntity");

        List<String> createContainerBE = new ArrayList<>();
        createContainerBE.add("FluidTankBlockEntity");
        createContainerBE.add("ItemVaultBlockEntity");
        createContainerBE.add("DyedContainerBE");

        for (BlockEntity blockEntity : blockEntities) {

            String blockEntityClass = blockEntity.getClass().getSimpleName();

            if (blockEntity instanceof GraffitiBlockEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(graffitiBlockHolder, objective);
                score.set(score.get() + 1);
                graffitiBlock++;
                totalBlockEntityScore++;
            } else if (blockEntityClass.contains("WallLanternBlockTile")) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(wallLanternHolder, objective);
                score.set(score.get() + 1);
                wallLantern++;
                totalBlockEntityScore++;
            } else if (blockEntity instanceof ChestBlockEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(chestHolder, objective);
                score.set(score.get() + 1);
                chest++;
                totalBlockEntityScore++;
            } else if (blockEntity instanceof BarrelBlockEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(barrelHolder, objective);
                score.set(score.get() + 1);
                barrel++;
                totalBlockEntityScore++;
            } else if (blockEntity instanceof SculkCatalystBlockEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(sculkHolder, objective);
                score.set(score.get() + 1);
                sculk++;
                totalBlockEntityScore++;
            } else if (blockEntity instanceof SculkSensorBlockEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(sculkHolder, objective);
                score.set(score.get() + 1);
                sculk++;
                totalBlockEntityScore++;
            } else if (blockEntity instanceof SculkShriekerBlockEntity) {
                ScoreAccess score = scoreboard.getOrCreatePlayerScore(sculkHolder, objective);
                score.set(score.get() + 1);
                sculk++;
                totalBlockEntityScore++;
            } else {
                boolean found = false;
                for (String className : copycatBE) {
                    if (!found) {
                        if (blockEntityClass.contains(className)) {
                            ScoreAccess score = scoreboard.getOrCreatePlayerScore(copyCatHolder, objective);
                            score.set(score.get() + 1);
                            copycat++;
                            totalBlockEntityScore++;
                            found = true;
                        }
                    }
                }
                if (!found) {
                    for (String className : createBE) {
                        if (!found) {
                            if (blockEntityClass.contains(className)) {
                                ScoreAccess score = scoreboard.getOrCreatePlayerScore(createHolder, objective);
                                score.set(score.get() + 1);
                                create++;
                                totalBlockEntityScore++;
                                found = true;
                            }
                        }
                    }
                }
                if (!found) {
                    for (String className : createContainerBE) {
                        if (!found) {
                            if (blockEntityClass.contains(className)) {
                                ScoreAccess score = scoreboard.getOrCreatePlayerScore(createContainerHolder, objective);
                                score.set(score.get() + 1);
                                createContainer++;
                                totalBlockEntityScore++;
                                found = true;
                            }
                        }
                    }
                }
                if (!found) {
                    IMBMod.LOGGER.info("BlockEntityName: " + blockEntityClass);
                    ScoreAccess score = scoreboard.getOrCreatePlayerScore(otherBlockEntityHolder, objective);
                    score.set(score.get() + 1);
                    otherBlockEntity++;
                    totalBlockEntityScore++;
                }
            }
        }
        ScoreAccess totalBlockEntityScoreAccess = scoreboard.getOrCreatePlayerScore(totalBlockEntityHolder, objective);
        totalBlockEntityScoreAccess.set(totalBlockEntityScore);

        int totalBEScore = graffitiBlock + wallLantern + chest + barrel + sculk + copycat + create + createContainer + otherBlockEntity;
        IMBMod.LOGGER.info("Total Block Entities: " + totalBlockEntityScore + " Check: " + totalBEScore);

        ScoreAccess totalTotalScoreAccess = scoreboard.getOrCreatePlayerScore(totalTotalHolder, objective);
        totalTotalScoreAccess.set(totalEntityScore + totalBlockEntityScore);

        int totalTotalScore = totalEntityScore + totalBlockEntityScore;
        int totalCheckScore = totalEScore + totalBEScore;
        IMBMod.LOGGER.info("Total Total Entities: " + totalTotalScore + " Check: " + totalCheckScore);

        IMBMod.LOGGER.info("Stats Done");
        return 1;
    }

    public static List<Entity> getNearbyEntities(Level level, Vec3 pos, double radius) {

        double x = pos.x();
        double y = pos.y();
        double z = pos.z();

        // Create a cubic bounding box (AABB) around the center
        AABB searchBox = new AABB(
                x - radius, y - radius, z - radius,
                x + radius, y + radius, z + radius
        );

        // Retrieve all entities within the bounding box
        return level.getEntities(null, searchBox);
    }


    public static List<BlockEntity> getNearbyBlockEntities(Level level, BlockPos center, int radius) {
        List<BlockEntity> blockEntities = new ArrayList<>();

        // Define bounds
        int minX = center.getX() - radius;
        int minY = Math.max(level.getMinBuildHeight(), center.getY() - radius);
        int minZ = center.getZ() - radius;
        int maxX = center.getX() + radius;
        int maxY = Math.min(level.getMaxBuildHeight(), center.getY() + radius);
        int maxZ = center.getZ() + radius;

        // Iterate through positions (efficient only for small radiuses)
        BlockPos.betweenClosedStream(minX, minY, minZ, maxX, maxY, maxZ).forEach(pos -> {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                blockEntities.add(blockEntity);
            }
        });

        return blockEntities;
    }
}
