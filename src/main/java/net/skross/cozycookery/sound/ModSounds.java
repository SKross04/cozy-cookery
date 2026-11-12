package net.skross.cozycookery.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, CozyCookery.MOD_ID);

    public static final Supplier<SoundEvent> SPOON_BONK = registerSoundEvent("spoon_bonk");
    public static final Supplier<SoundEvent> CHOPPING_BOARD_CHOP = registerSoundEvent("chopping_board_chop");
    public static final Supplier<SoundEvent> COOKING_POT_STIR = registerSoundEvent("cooking_pot_stir");
    public static final Supplier<SoundEvent> COOKING_POT_BLUB = registerSoundEvent("cooking_pot_blub");
    public static final Supplier<SoundEvent> FRYING_PAN_SIZZLE = registerSoundEvent("frying_pan_sizzle");
    public static final Supplier<SoundEvent> FRYING_PAN_FLIP = registerSoundEvent("frying_pan_flip");
    public static final Supplier<SoundEvent> MORTAR_GRIND = registerSoundEvent("mortar_grind");
    public static final Supplier<SoundEvent> OVEN_DING = registerSoundEvent("oven_ding");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
