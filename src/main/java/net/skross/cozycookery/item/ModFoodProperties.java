package net.skross.cozycookery.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    // Balancing rule: cutting meat never creates extra food.
    // saturationModifier 0.3 -> saturation = nutrition * 0.3 * 2 (same formula as vanilla)

    // Same values as vanilla raw beef / raw porkchop (3 hunger, 1.8 saturation)
    public static final FoodProperties RAW_BEEF_CUT = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.3f).build();
    public static final FoodProperties RAW_PORK_CUT = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.3f).build();

    // Same values as vanilla raw mutton (2 hunger, 1.2 saturation)
    public static final FoodProperties RAW_MUTTON_CUT = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3f).build();

    // Raw chicken pieces: 1 hunger, 0.6 saturation, 30% chance of Hunger (like raw chicken)
    public static final FoodProperties RAW_CHICKEN_PIECE = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.3f)
            .effect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3f).build();

    // Cooked chicken: 2 drumsticks + 2 wings = 6 hunger / 7.2 saturation = exactly vanilla cooked chicken
    public static final FoodProperties COOKED_CHICKEN_DRUMSTICK = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.6f).build();
    public static final FoodProperties COOKED_CHICKEN_WING = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.6f).build();
}
