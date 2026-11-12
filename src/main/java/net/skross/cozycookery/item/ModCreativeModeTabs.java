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
                        ModBlocks.CHOPPING_BOARDS.values().forEach(output::accept);
                        output.accept(ModBlocks.COOKING_POT);
                        output.accept(ModBlocks.FRYING_PAN);
                        output.accept(ModBlocks.MORTAR);
                        output.accept(ModBlocks.MIXING_BOWL);
                        ModBlocks.STOVES.values().forEach(output::accept);
                        ModBlocks.OVENS.values().forEach(output::accept);

                        output.accept(ModItems.FLINT_KNIFE);
                        output.accept(ModItems.IRON_KNIFE);
                        output.accept(ModItems.GOLDEN_KNIFE);
                        output.accept(ModItems.DIAMOND_KNIFE);
                        output.accept(ModItems.NETHERITE_KNIFE);
                        output.accept(ModItems.WOODEN_SPOON);
                        output.accept(ModItems.WOODEN_SPATULA);

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

                        output.accept(ModItems.CARROT_SLICES);
                        output.accept(ModItems.CARROT_CUBES);
                        output.accept(ModItems.POTATO_SLICES);
                        output.accept(ModItems.POTATO_CUBES);
                        output.accept(ModItems.BEETROOT_CUBES);
                        output.accept(ModItems.SLICED_MUSHROOMS);
                        output.accept(ModItems.APPLE_SLICES);
                        output.accept(ModItems.PUMPKIN_SLICE);

                        output.accept(ModItems.FLOUR);
                        output.accept(ModItems.BREAD_DOUGH);
                        output.accept(ModItems.COOKIE_DOUGH);
                        output.accept(ModItems.CAKE_BATTER);
                        output.accept(ModItems.RAW_PUMPKIN_PIE);
                        output.accept(ModItems.RAW_APPLE_PIE);
                        output.accept(ModItems.APPLE_PIE);
                        output.accept(ModItems.SEARED_BEEF_STRIPS);
                        output.accept(ModItems.SEARED_BEEF_CUBES);
                        output.accept(ModItems.BROWNED_GROUND_BEEF);
                        output.accept(ModItems.BACON);
                        output.accept(ModItems.FRIED_PORK_CUBES);
                        output.accept(ModItems.BROWNED_GROUND_PORK);
                        output.accept(ModItems.SEARED_MUTTON_STRIPS);
                        output.accept(ModItems.SEARED_MUTTON_CUBES);
                        output.accept(ModItems.BROWNED_GROUND_MUTTON);
                        output.accept(ModItems.POTATO_CHIPS);
                        output.accept(ModItems.FRIED_POTATO_CUBES);
                        output.accept(ModItems.FRIED_MUSHROOMS);
                        output.accept(ModItems.FRIED_EGG);
                        output.accept(ModItems.CHARRED_SCRAPS);

                        output.accept(ModItems.BEEF_STEW);
                        output.accept(ModItems.CHICKEN_SOUP);
                        output.accept(ModItems.VEGETABLE_SOUP);
                        output.accept(ModItems.PUMPKIN_SOUP);
                        output.accept(ModItems.BURNT_STEW);
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
