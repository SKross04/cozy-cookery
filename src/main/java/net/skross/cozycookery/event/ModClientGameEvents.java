package net.skross.cozycookery.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.skross.cozycookery.client.CloseUpCamera;
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

    // Close-up mode: no floating hand and no hotbar/hearts, only the station and your inventory panel
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (CloseUpCamera.isActive()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (CloseUpCamera.isActive()) {
            event.setCanceled(true);
        }
    }

    // Close-up mode: your own character is hidden, so your head never blocks the view of the station
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (CloseUpCamera.isActive() && event.getEntity() == Minecraft.getInstance().player) {
            event.setCanceled(true);
        }
    }
}
