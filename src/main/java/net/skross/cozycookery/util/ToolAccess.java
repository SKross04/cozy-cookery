package net.skross.cozycookery.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.entity.ToolRackBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Where the tool for a close-up comes from: your main hand, or a hook on a nearby tool rack.
 * rack == null means "main hand".
 */
public record ToolAccess(@Nullable BlockPos rack, int slot) {
    public static final ToolAccess HAND = new ToolAccess(null, -1);
    private static final int SEARCH_RADIUS = 4;   // blocks around the station (horizontal)
    private static final int SEARCH_HEIGHT = 2;   // blocks above and below the station

    public boolean fromRack() {
        return rack != null;
    }

    public ItemStack get(Player player) {
        if (rack == null) {
            return player.getMainHandItem();
        }
        if (player.level().getBlockEntity(rack) instanceof ToolRackBlockEntity toolRack) {
            return toolRack.getTool(slot);
        }
        return ItemStack.EMPTY;
    }

    /** 1 durability off the tool (server side). */
    public void damage(Player player) {
        ItemStack tool = get(player);
        if (tool.isEmpty()) {
            return;
        }
        if (rack == null) {
            tool.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            return;
        }
        if (player.level() instanceof ServerLevel serverLevel
                && player.level().getBlockEntity(rack) instanceof ToolRackBlockEntity toolRack) {
            tool.hurtAndBreak(1, serverLevel, player instanceof ServerPlayer sp ? sp : null, item ->
                    serverLevel.playSound(null, rack, SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8f, 1.0f));
            toolRack.toolsChanged();
        }
    }

    /**
     * The right tool for a station: in your main hand first, otherwise the first one hanging on a tool rack nearby.
     * Returns null if there is none (then the close-up opens in "add ingredients only" mode).
     */
    @Nullable
    public static ToolAccess find(Player player, BlockPos station, Predicate<ItemStack> isTool) {
        if (isTool.test(player.getMainHandItem())) {
            return HAND;
        }
        for (BlockPos p : BlockPos.betweenClosed(station.offset(-SEARCH_RADIUS, -SEARCH_HEIGHT, -SEARCH_RADIUS),
                station.offset(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS))) {
            if (player.level().getBlockEntity(p) instanceof ToolRackBlockEntity toolRack) {
                for (int i = 0; i < ToolRackBlockEntity.SLOTS; i++) {
                    if (isTool.test(toolRack.getTool(i))) {
                        return new ToolAccess(p.immutable(), i);
                    }
                }
            }
        }
        return null;
    }

    // ---------- Network (server -> client, when the close-up opens) ----------
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(rack != null);
        if (rack != null) {
            buf.writeBlockPos(rack);
            buf.writeVarInt(slot);
        }
    }

    public static ToolAccess read(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return HAND;
        }
        return new ToolAccess(buf.readBlockPos(), buf.readVarInt());
    }
}
