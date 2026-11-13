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

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
