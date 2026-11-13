package net.skross.cozycookery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.custom.MortarBlock;
import net.skross.cozycookery.block.entity.MortarBlockEntity;
import net.skross.cozycookery.menu.MortarMenu;
import net.skross.cozycookery.sound.ModSounds;

/** Client -> server: one full circle ground with the pestle. 3 circles grind the item. */
public record GrindPayload(BlockPos pos) implements CustomPacketPayload {
    public static final int TURNS_NEEDED = 3;
    public static final Type<GrindPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, "grind"));
    public static final StreamCodec<ByteBuf, GrindPayload> STREAM_CODEC =
            BlockPos.STREAM_CODEC.map(GrindPayload::new, GrindPayload::pos);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GrindPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof MortarMenu menu) || !menu.pos.equals(payload.pos())
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        MortarBlockEntity mortar = menu.mortar;
        if (mortar.isEmpty() || MortarBlock.findRecipe(level, mortar.getItem()).isEmpty()) {
            mortar.grindTurns = 0;
            return;
        }
        level.playSound(null, payload.pos(), ModSounds.MORTAR_GRIND.get(), SoundSource.BLOCKS,
                0.8f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, mortar.getItem().copy()),
                payload.pos().getX() + 0.5, payload.pos().getY() + 0.3, payload.pos().getZ() + 0.5,
                3, 0.08, 0.03, 0.08, 0.02);
        mortar.grindTurns++;
        if (mortar.grindTurns >= TURNS_NEEDED) {
            mortar.grindTurns = 0;
            MortarBlock.findRecipe(level, mortar.getItem())
                    .ifPresent(recipe -> MortarBlock.grind(recipe, mortar, level, payload.pos(), player));
        }
    }
}
