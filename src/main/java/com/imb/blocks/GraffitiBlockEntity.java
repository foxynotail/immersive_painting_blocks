package com.imb.blocks;

import com.imb.registry.IMBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class GraffitiBlockEntity extends BlockEntity {
    private ResourceLocation motive;
    private int paintingWidth;
    private int paintingHeight;
    private int thickness = 1;
    private boolean isGlowing = false;
    private float yaw = 0.0f;
    private float shiftX = 0.0f;
    private float shiftY = 0.0f;
    private float shiftZ = 0.0f;
    private final int maxThickness = 64;
    public static final float thicknessGap = 0.004F;
    public int lightLevel = 0;

    public GraffitiBlockEntity(BlockPos pos, BlockState state) {
        super(IMBBlockEntities.GRAFFITI_BLOCK_ENTITY.get(), pos, state);
    }

    public void setGraffitiData(ResourceLocation motive, int width, int height, boolean isGlowing, float yaw, float shiftX, float shiftY, float shiftZ) {
        this.motive = motive;
        this.paintingWidth = width;
        this.paintingHeight = height;
        this.isGlowing = isGlowing;
        this.yaw = yaw;
        this.shiftX = shiftX;
        this.shiftY = shiftY;
        this.shiftZ = shiftZ;
        setChanged();
    }

    public void incrementThickness() {

        if (this.thickness >= this.maxThickness) {
            this.thickness = this.maxThickness;
            return;
        }

        this.thickness++;

        setThickness(this.thickness);
    }

    public void setThickness(int thickness) {
        if (thickness >= this.maxThickness) {
            thickness = this.maxThickness;
        }
        if (thickness < 1) thickness = 1;

        this.thickness = thickness;
        if (this.isGlowing && this.level != null) {

            int lightLevel = Mth.ceil(((float) this.thickness / this.maxThickness) * 16) - 1;
            if (lightLevel < 0) lightLevel = 0;
            if (lightLevel > 15) lightLevel = 15;

            this.lightLevel = lightLevel;

            BlockState currentState = this.getBlockState();
            if (currentState.getValue(GraffitiBlock.LIGHT_LEVEL) != this.lightLevel) {
                this.level.setBlockAndUpdate(this.getBlockPos(), currentState.setValue(GraffitiBlock.LIGHT_LEVEL, this.lightLevel));
            }
        }
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public ResourceLocation getMotive() {
        return motive;
    }

    public int getPaintingWidth() {
        return paintingWidth;
    }

    public int getPaintingHeight() {
        return paintingHeight;
    }

    public int getThickness() {
        return thickness;
    }

    public boolean isGlowing() {
        return isGlowing;
    }

    public float getYaw() {
        return yaw;
    }

    public float getShiftX() {
        return shiftX;
    }

    public float getShiftY() {
        return shiftY;
    }

    public float getShiftZ() {
        return shiftZ;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.motive != null) {
            tag.putString("Motive", this.motive.toString());
        }
        tag.putInt("PaintingWidth", this.paintingWidth);
        tag.putInt("PaintingHeight", this.paintingHeight);
        tag.putInt("Thickness", this.thickness);
        tag.putBoolean("IsGlowing", this.isGlowing);
        tag.putFloat("Yaw", this.yaw);
        tag.putFloat("ShiftX", this.shiftX);
        tag.putFloat("ShiftY", this.shiftY);
        tag.putFloat("ShiftZ", this.shiftZ);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Motive")) {
            this.motive = ResourceLocation.parse(tag.getString("Motive"));
        }
        this.paintingWidth = tag.getInt("PaintingWidth");
        this.paintingHeight = tag.getInt("PaintingHeight");
        this.thickness = tag.getInt("Thickness");
        this.isGlowing = tag.getBoolean("IsGlowing");
        this.yaw = tag.getFloat("Yaw");
        this.shiftX = tag.getFloat("ShiftX");
        this.shiftY = tag.getFloat("ShiftY");
        this.shiftZ = tag.getFloat("ShiftZ");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt, HolderLookup.@NotNull Provider registries) {
        super.onDataPacket(net, pkt, registries);
        loadAdditional(pkt.getTag(), registries);
    }


}