package net.skross.cozycookery.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.skross.cozycookery.CozyCookery;

import java.util.Map;
import java.util.Optional;

// Mod loading events (things that happen once while the game starts)
@EventBusSubscriber(modid = CozyCookery.MOD_ID)
public class ModBusEvents {

    // Vanilla stews stack to 4, like our own soups.
    // We also remove vanilla's own "give the bowl back" (it has stacking bugs in 1.21.1);
    // ModEvents.onFinishEating gives the bowl back instead.
    @SubscribeEvent
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        Map<Item, FoodProperties> stews = Map.of(
                Items.MUSHROOM_STEW, Foods.MUSHROOM_STEW,
                Items.BEETROOT_SOUP, Foods.BEETROOT_SOUP,
                Items.RABBIT_STEW, Foods.RABBIT_STEW,
                Items.SUSPICIOUS_STEW, Foods.SUSPICIOUS_STEW);

        stews.forEach((stew, food) -> event.modify(stew, builder -> {
            builder.set(DataComponents.MAX_STACK_SIZE, 4);
            // same food values, but without vanilla's "converts to bowl"
            builder.set(DataComponents.FOOD, new FoodProperties(food.nutrition(), food.saturation(),
                    food.canAlwaysEat(), food.eatSeconds(), Optional.empty(), food.effects()));
        }));
    }
}
