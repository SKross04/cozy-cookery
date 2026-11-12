package net.skross.cozycookery.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CozyCookery.MOD_ID);

    public static final Supplier<CreativeModeTab> COZY_COOKERY_TAB = CREATIVE_MODE_TAB.register("cozy_cookery_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_KNIFE.get()))
                    .title(Component.translatable("creativetab.cozycookery.cozy_cookery_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.CHOPPING_BOARD);

                        output.accept(ModItems.FLINT_KNIFE);
                        output.accept(ModItems.IRON_KNIFE);
                        output.accept(ModItems.GOLDEN_KNIFE);
                        output.accept(ModItems.DIAMOND_KNIFE);
                        output.accept(ModItems.NETHERITE_KNIFE);
                        output.accept(ModItems.WOODEN_SPOON);

                        output.accept(ModItems.RAW_BEEF_STRIPS);
                        output.accept(ModItems.RAW_BEEF_CUBES);
                        output.accept(ModItems.GROUND_BEEF);
                        output.accept(ModItems.RAW_PORK_STRIPS);
                        output.accept(ModItems.RAW_PORK_CUBES);
                        output.accept(ModItems.GROUND_PORK);
                        output.accept(ModItems.RAW_MUTTON_STRIPS);
                        output.accept(ModItems.RAW_MUTTON_CUBES);
                        output.accept(ModItems.GROUND_MUTTON);
                        output.accept(ModItems.RAW_CHICKEN_DRUMSTICK);
                        output.accept(ModItems.RAW_CHICKEN_WING);
                        output.accept(ModItems.COOKED_CHICKEN_DRUMSTICK);
                        output.accept(ModItems.COOKED_CHICKEN_WING);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
