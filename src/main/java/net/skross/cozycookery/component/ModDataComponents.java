package net.skross.cozycookery.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, CozyCookery.MOD_ID);

    // "cozycookery:soup_contents": soup carried in a mixing bowl
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SoupContents>> SOUP_CONTENTS =
            DATA_COMPONENT_TYPES.register("soup_contents", () -> DataComponentType.<SoupContents>builder()
                    .persistent(SoupContents.CODEC)
                    .networkSynchronized(SoupContents.STREAM_CODEC)
                    .build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
