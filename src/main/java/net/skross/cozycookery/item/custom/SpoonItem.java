package net.skross.cozycookery.item.custom;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.skross.cozycookery.sound.ModSounds;

import java.util.Optional;

public class SpoonItem extends Item {
    // Later the cooking pot will check "instanceof SpoonItem" and wear it down with every stir.
    public SpoonItem(Properties properties) {
        super(properties);
    }

    // Called every time you hit an entity with the spoon (and the hit actually did damage).
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();

        if (!level.isClientSide()) {
            // Bonk! Only when a player gets hit on the head.
            if (target instanceof Player && isHeadHit(target, attacker)) {
                level.playSound(null, target.getX(), target.getEyeY(), target.getZ(),
                        ModSounds.SPOON_BONK.get(), SoundSource.PLAYERS,
                        1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
            }

            // Every hit costs 1 durability, like a sword.
            stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        }

        return true;
    }

    // Draws a line from the attacker's eyes in the direction they're looking,
    // and checks where that line enters the target's hitbox.
    // If it's in the top quarter of the hitbox, it counts as the head.
    private static boolean isHeadHit(LivingEntity target, LivingEntity attacker) {
        Vec3 eyePos = attacker.getEyePosition();
        Vec3 lookEnd = eyePos.add(attacker.getLookAngle().scale(6.0));

        Optional<Vec3> hitPos = target.getBoundingBox().inflate(0.1).clip(eyePos, lookEnd);
        return hitPos.isPresent()
                && hitPos.get().y >= target.getY() + target.getBbHeight() * 0.75;
    }
}
