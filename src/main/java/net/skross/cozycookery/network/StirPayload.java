package net.skross.cozycookery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.item.custom.SpoonItem;
import net.skross.cozycookery.menu.CloseUpMode;
import net.skross.cozycookery.menu.CookingPotMenu;
import net.skross.cozycookery.sound.ModSounds;

/**
 * Client -> server: "I moved the spoon a quarter turn around this pot" (fullTurn = this quarter completed a circle).
 * The server checks that you really are in that pot's close-up with a spoon before it counts.
 */
public record StirPayload(BlockPos pos, boolean fullTurn) implements CustomPacketPayload {
    public static final Type<StirPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, "stir"));

    public static final StreamCodec<ByteBuf, StirPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, StirPayload::pos,
            ByteBufCodecs.BOOL, StirPayload::fullTurn,
            StirPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Runs on the server's main thread
    public static void handle(StirPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof CookingPotMenu menu)
                || menu.mode != CloseUpMode.TOOL || !menu.pos.equals(payload.pos())) {
            return;
        }
        ItemStack spoon = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(spoon.getItem() instanceof SpoonItem) || !menu.pot.stir(player.level())) {
            return;
        }
        if (payload.fullTurn()) {
            player.level().playSound(null, payload.pos(), ModSounds.COOKING_POT_STIR.get(), SoundSource.BLOCKS,
                    1.0f, 0.9f + player.level().getRandom().nextFloat() * 0.2f);
            spoon.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            if (spoon.isEmpty()) {
                player.closeContainer(); // the spoon broke
            }
        }
    }
}
