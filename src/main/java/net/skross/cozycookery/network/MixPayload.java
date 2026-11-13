package net.skross.cozycookery.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.entity.MixingBowlBlockEntity;
import net.skross.cozycookery.item.custom.SpoonItem;
import net.skross.cozycookery.menu.CloseUpMode;
import net.skross.cozycookery.menu.MixingBowlMenu;
import net.skross.cozycookery.recipe.MixingRecipe;
import net.skross.cozycookery.sound.ModSounds;

import java.util.Optional;

/** Client -> server: a quarter circle stirred in a mixing bowl. 3 full circles turn the ingredients into dough. */
public record MixPayload(BlockPos pos, boolean fullTurn) implements CustomPacketPayload {
    public static final int QUARTERS_NEEDED = 12;
    public static final Type<MixPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, "mix"));
    public static final StreamCodec<ByteBuf, MixPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MixPayload::pos, ByteBufCodecs.BOOL, MixPayload::fullTurn, MixPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MixPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.containerMenu instanceof MixingBowlMenu menu)
                || menu.mode != CloseUpMode.TOOL || !menu.pos.equals(payload.pos())
                || !(menu.tool.get(player).getItem() instanceof SpoonItem)
                || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        MixingBowlBlockEntity bowl = menu.bowl;
        if (bowl.isEmpty() || bowl.isMixed() || bowl.hasSoup()) {
            return;
        }
        if (payload.fullTurn()) {
            level.playSound(null, payload.pos(), ModSounds.COOKING_POT_STIR.get(), SoundSource.BLOCKS,
                    1.0f, 1.1f + level.getRandom().nextFloat() * 0.2f);
            menu.tool.damage(player);
        }
        Optional<MixingRecipe> recipe = bowl.findRecipe(level);
        if (recipe.isEmpty()) {
            bowl.mixQuarters = 0; // these ingredients don't make anything (yet)
            return;
        }
        bowl.mixQuarters++;
        if (bowl.mixQuarters >= QUARTERS_NEEDED) {
            bowl.mixQuarters = 0;
            ItemStack particleItem = bowl.items.getStackInSlot(0).copy();
            bowl.mix(recipe.get());
            level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleItem),
                    payload.pos().getX() + 0.5, payload.pos().getY() + 0.4, payload.pos().getZ() + 0.5,
                    8, 0.15, 0.05, 0.15, 0.03);
        }
        if (menu.tool.get(player).isEmpty()) {
            player.closeContainer(); // the spoon broke
        }
    }
}
