package net.skross.cozycookery.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.util.ModTags;

// Game events (things that happen while playing)
@EventBusSubscriber(modid = CozyCookery.MOD_ID)
public class ModEvents {

    // After eating a bowl food (tag cozycookery:bowl_foods), you always get the empty bowl back,
    // also when you eat one from a stack of 4.
    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        ItemStack eaten = event.getItem(); // a copy of the stack from before eating
        if (!eaten.is(ModTags.Items.BOWL_FOODS)) {
            return;
        }
        if (!(event.getEntity() instanceof Player player) || player.hasInfiniteMaterials()) {
            return; // creative mode doesn't use up the food, so no extra bowl
        }
        ItemStack bowl = new ItemStack(Items.BOWL);
        if (event.getResultStack().isEmpty()) {
            event.setResultStack(bowl);          // last one eaten -> the bowl stays in your hand
        } else if (!player.level().isClientSide()) {
            if (!player.getInventory().add(bowl)) {
                player.drop(bowl, false);        // inventory full -> drop it
            }
        }
    }
}
