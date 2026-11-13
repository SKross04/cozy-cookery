package net.skross.cozycookery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.item.custom.SpatulaItem;
import net.skross.cozycookery.menu.CloseUpMode;
import net.skross.cozycookery.menu.FryingPanMenu;
import net.skross.cozycookery.sound.ModSounds;

// Client -> server: "I flicked the spatula in this pan"
public record FlipPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<FlipPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, "flip"));
    public static final StreamCodec<ByteBuf, FlipPayload> STREAM_CODEC =
            BlockPos.STREAM_CODEC.map(FlipPayload::new, FlipPayload::pos);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FlipPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof FryingPanMenu menu)
                || menu.mode != CloseUpMode.TOOL || !menu.pos.equals(payload.pos())
                || !(menu.tool.get(player).getItem() instanceof SpatulaItem)) {
            return;
        }
        player.level().playSound(null, payload.pos(), ModSounds.FRYING_PAN_FLIP.get(), SoundSource.BLOCKS,
                1.0f, 0.9f + player.level().getRandom().nextFloat() * 0.2f);
        if (menu.pan.flip()) {
            menu.tool.damage(player);
            if (menu.tool.get(player).isEmpty()) {
                player.closeContainer(); // the spatula broke
            }
        }
    }
}
