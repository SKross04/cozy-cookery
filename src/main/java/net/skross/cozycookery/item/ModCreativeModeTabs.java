package net.skross.cozycookery.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CozyCookery.MOD_ID);

    public static final Supplier<CreativeModeTab> COZY_COOKERY_TAB = CREATIVE_MODE_TAB.register("cozy_cookery_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_KNIFE.get()))
                    .title(Component.translatable("creativetab.cozycookery.cozy_cookery_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.FLINT_KNIFE);
                        output.accept(ModItems.IRON_KNIFE);
                        output.accept(ModItems.GOLDEN_KNIFE);
                        output.accept(ModItems.DIAMOND_KNIFE);
                        output.accept(ModItems.NETHERITE_KNIFE);
                        output.accept(ModItems.WOODEN_SPOON);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
