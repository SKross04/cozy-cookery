package net.skross.cozycookery.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.component.ModDataComponents;
import net.skross.cozycookery.component.SoupContents;

// Game events that only happen on the client (screens, tooltips...)
@EventBusSubscriber(modid = CozyCookery.MOD_ID, value = Dist.CLIENT)
public class ModClientGameEvents {

    // Tooltip on a filled mixing bowl: "2x Beef Stew"
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        SoupContents contents = event.getItemStack().get(ModDataComponents.SOUP_CONTENTS.get());
        if (contents != null) {
            event.getToolTip().add(Component.translatable("tooltip.cozycookery.soup_contents",
                    contents.servings(), contents.serving().getDescription()).withStyle(ChatFormatting.GRAY));
        }
    }
}
