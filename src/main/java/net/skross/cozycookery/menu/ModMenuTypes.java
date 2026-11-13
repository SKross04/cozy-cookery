package net.skross.cozycookery.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CozyCookery.MOD_ID);

    // IMenuTypeExtension.create lets the server send extra data (the pot's position + the mode) to the client
    public static final DeferredHolder<MenuType<?>, MenuType<CookingPotMenu>> COOKING_POT_MENU =
            MENUS.register("cooking_pot", () -> IMenuTypeExtension.create(CookingPotMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<FryingPanMenu>> FRYING_PAN_MENU =
            MENUS.register("frying_pan", () -> IMenuTypeExtension.create(FryingPanMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ChoppingBoardMenu>> CHOPPING_BOARD_MENU =
            MENUS.register("chopping_board", () -> IMenuTypeExtension.create(ChoppingBoardMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MixingBowlMenu>> MIXING_BOWL_MENU =
            MENUS.register("mixing_bowl", () -> IMenuTypeExtension.create(MixingBowlMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MortarMenu>> MORTAR_MENU =
            MENUS.register("mortar", () -> IMenuTypeExtension.create(MortarMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<OvenMenu>> OVEN_MENU =
            MENUS.register("oven", () -> IMenuTypeExtension.create(OvenMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<PlateMenu>> PLATE_MENU =
            MENUS.register("plate", () -> IMenuTypeExtension.create(PlateMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
