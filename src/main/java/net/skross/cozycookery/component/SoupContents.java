package net.skross.cozycookery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

/**
 * Data stored ON an item: "this mixing bowl contains 2x Beef Stew, brown colour".
 * A data component must be immutable and have a proper equals(), so we store the Item (not an ItemStack).
 *
 * CODEC:        how it's saved (world save, item in a chest...)
 * STREAM_CODEC: how it's sent from the server to the client
 */
public record SoupContents(Item serving, int servings, int color) {
    public static final Codec<SoupContents> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("serving").forGetter(SoupContents::serving),
            Codec.INT.fieldOf("servings").forGetter(SoupContents::servings),
            Codec.INT.fieldOf("color").forGetter(SoupContents::color)
    ).apply(inst, SoupContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoupContents> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), SoupContents::serving,
            ByteBufCodecs.VAR_INT, SoupContents::servings,
            ByteBufCodecs.INT, SoupContents::color,
            SoupContents::new);
}
