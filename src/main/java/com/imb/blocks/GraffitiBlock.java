package com.imb.blocks;

import com.imb.registry.IMBItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraffitiBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty GLOWING = BooleanProperty.create("glowing");
    public static final IntegerProperty LIGHT_LEVEL = IntegerProperty.create("light_level", 0, 15);

    public GraffitiBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(GLOWING, false)
                .setValue(LIGHT_LEVEL, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, GLOWING, LIGHT_LEVEL);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    private VoxelShape makeShape(@NotNull BlockState state, BlockGetter level, @NotNull BlockPos pos) {
        float depth = 0.0625f; // Default 1-pixel fallback for simulated queries (like Create Mod)

        // Safely attempt to read exact thickness if the BlockEntity is loaded
        if (level != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GraffitiBlockEntity graffiti) {
                depth = Math.max(0.0625f, graffiti.getThickness() * 0.00625f);
            }
        }

        Direction facing = state.getValue(FACING);

        return switch (facing) {
            case SOUTH -> Block.box(0, 0, 0, 16, 16, depth * 16);
            case NORTH -> Block.box(0, 0, (1 - depth) * 16, 16, 16, 16);
            case WEST -> Block.box((1 - depth) * 16, 0, 0, 16, 16, 16);
            case EAST -> Block.box(0, 0, 0, depth * 16, 16, 16);
            case UP -> Block.box(0, 0, 0, 16, depth * 16, 16);
            case DOWN -> Block.box(0, (1 - depth) * 16, 0, 16, 16, 16);
        };
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return makeShape(state, level, pos);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return makeShape(state, level, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new GraffitiBlockEntity(pos, state);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (stack.is(IMBItems.GRAFFITI_CONVERTER_ITEM)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GraffitiBlockEntity graffitiEntity) {
                if (!level.isClientSide()) {
                    graffitiEntity.incrementThickness();
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}