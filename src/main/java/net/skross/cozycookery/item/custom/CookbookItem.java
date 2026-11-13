package net.skross.cozycookery.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.skross.cozycookery.client.ClientHooks;

// Right-click: read the cookbook. Sneak + right-click on a block: lay it down open on the counter.
public class CookbookItem extends BlockItem {
    public CookbookItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            ClientHooks.openCookbook();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return super.useOn(context); // place it
        }
        if (context.getLevel().isClientSide()) {
            ClientHooks.openCookbook();
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
