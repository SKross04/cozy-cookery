package net.skross.cozycookery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.custom.ChoppingBoardBlock;
import net.skross.cozycookery.menu.ChoppingBoardMenu;
import net.skross.cozycookery.menu.CloseUpMode;

// Client -> server: "I brought the knife down on this board"
public record ChopPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ChopPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, "chop"));
    public static final StreamCodec<ByteBuf, ChopPayload> STREAM_CODEC =
            BlockPos.STREAM_CODEC.map(ChopPayload::new, ChopPayload::pos);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ChopPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof ChoppingBoardMenu menu)
                || menu.mode != CloseUpMode.TOOL || !menu.pos.equals(payload.pos())
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ChoppingBoardBlock.tryChop(level, menu.pos, menu.board, player, menu.tool, true);
        if (menu.tool.get(player).isEmpty()) {
            player.closeContainer(); // the knife broke
        }
    }
}
