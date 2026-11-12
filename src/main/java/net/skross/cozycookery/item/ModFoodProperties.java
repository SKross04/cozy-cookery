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

    // ---------- Pot dishes (2 bowls per pot; together worth about the cooked ingredients + a small bonus) ----------
    // Beef stew: beef cubes + potato + carrot (cooked worth 8 + 5 + 3 = 16) -> 2 x 8
    public static final FoodProperties BEEF_STEW = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.75f).build();
    // Chicken soup: 2 drumsticks + 2 wings + carrot + potato (6 + 3 + 5 = 14) -> 2 x 7
    public static final FoodProperties CHICKEN_SOUP = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.7f).build();
    // Burnt stew: you forgot to stir... barely edible, 50% chance of Nausea
    public static final FoodProperties BURNT_STEW = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.1f)
            .effect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 0.5f).build();

    // ---------- Chopped vegetables & fruit (same value as what you chopped) ----------
    public static final FoodProperties CARROT_CUT = new FoodProperties.Builder()      // like a carrot
            .nutrition(3).saturationModifier(0.6f).build();
    public static final FoodProperties POTATO_CUT = new FoodProperties.Builder()      // like a raw potato
            .nutrition(1).saturationModifier(0.3f).build();
    public static final FoodProperties BEETROOT_CUT = new FoodProperties.Builder()    // like a beetroot
            .nutrition(1).saturationModifier(0.6f).build();
    public static final FoodProperties APPLE_SLICES = new FoodProperties.Builder()    // like an apple
            .nutrition(4).saturationModifier(0.3f).build();
    public static final FoodProperties PUMPKIN_SLICE = new FoodProperties.Builder()   // 4 slices from 1 pumpkin
            .nutrition(1).saturationModifier(0.3f).build();

    // Vegetable soup: carrot + potato + beetroot + mushrooms (cooked 3 + 5 + 1 + 0 = 9) -> 2 x 5
    public static final FoodProperties VEGETABLE_SOUP = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6f).build();
    // Pumpkin soup: 4 pumpkin slices + carrot + potato (4 + 3 + 5 = 12) -> 2 x 6
    public static final FoodProperties PUMPKIN_SOUP = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6f).build();

    // ---------- Fried in the pan (same value as the vanilla cooked meat they come from) ----------
    public static final FoodProperties SEARED_BEEF = new FoodProperties.Builder()     // like steak
            .nutrition(8).saturationModifier(0.8f).build();
    public static final FoodProperties FRIED_PORK = new FoodProperties.Builder()      // like a cooked porkchop
            .nutrition(8).saturationModifier(0.8f).build();
    public static final FoodProperties SEARED_MUTTON = new FoodProperties.Builder()   // like cooked mutton
            .nutrition(6).saturationModifier(0.8f).build();
    public static final FoodProperties FRIED_POTATO = new FoodProperties.Builder()    // like a baked potato
            .nutrition(5).saturationModifier(0.6f).build();
    public static final FoodProperties FRIED_MUSHROOMS = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3f).build();
    public static final FoodProperties FRIED_EGG = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.6f).build();
    // You forgot to flip it... 30% chance of Hunger
    public static final FoodProperties CHARRED_SCRAPS = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.1f)
            .effect(new MobEffectInstance(MobEffects.HUNGER, 300, 0), 0.3f).build();

    // Apple pie: like vanilla pumpkin pie (8 hunger), a tiny bit more saturating
    public static final FoodProperties APPLE_PIE = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.35f).build();

    // ---------- Crops ----------
    public static final FoodProperties TOMATO = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.4f).build();
    public static final FoodProperties CABBAGE = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.5f).build();
    public static final FoodProperties ONION = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3f).build();
    public static final FoodProperties GARLIC = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.3f).build();

    // ---------- New crops in the kitchen ----------
    public static final FoodProperties CABBAGE_LEAF = new FoodProperties.Builder()     // 4 leaves from 1 cabbage
            .nutrition(1).saturationModifier(0.5f).build();
    public static final FoodProperties BREAD_SLICE = new FoodProperties.Builder()      // 4 slices from 1 bread
            .nutrition(1).saturationModifier(0.6f).build();
    public static final FoodProperties TOAST = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.6f).build();
    public static final FoodProperties FRIED_ONIONS = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.5f).build();
    public static final FoodProperties GARLIC_BREAD = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.6f).build();
    // Tomato soup: 4 tomatoes + onion + garlic (12 + 2 + 0 = 14) -> 2 x 7
    public static final FoodProperties TOMATO_SOUP = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.6f).build();
    // Onion soup: 3 onion rings + garlic + bread slice (6 + 0 + 1 = 7) -> 2 x 5
    public static final FoodProperties ONION_SOUP = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6f).build();
    // Cabbage soup: cabbage + carrot + onion + potato (1 + 3 + 2 + 5 = 11) -> 2 x 6
    public static final FoodProperties CABBAGE_SOUP = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6f).build();
    // Garden salad: cabbage leaf + tomato + onion + carrot (1 + 3 + 2 + 3 = 9) -> 2 x 5
    public static final FoodProperties GARDEN_SALAD = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.6f).build();
    // Coleslaw: cabbage + carrot + egg (1 + 3 + 3 = 7) -> 2 x 4
    public static final FoodProperties COLESLAW = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6f).build();

    // ---------- Flour dishes & more ----------
    public static final FoodProperties COOKED_NOODLES = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.4f).build();
    public static final FoodProperties PANCAKE = new FoodProperties.Builder()        // 4 from flour + egg + milk + sugar
            .nutrition(3).saturationModifier(0.6f).build();
    public static final FoodProperties MEATBALLS = new FoodProperties.Builder()      // 4 from ground pork + bread + egg + garlic
            .nutrition(2).saturationModifier(0.6f).build();
    public static final FoodProperties OMELETTE = new FoodProperties.Builder()       // 2 eggs + onion + tomato
            .nutrition(7).saturationModifier(0.6f).build();
    // Spaghetti pomodoro: noodles + 2 tomatoes + onion (3 + 6 + 2 = 11) -> 2 x 6
    public static final FoodProperties SPAGHETTI_POMODORO = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6f).build();
    // Spaghetti bolognese: noodles + beef + 2 tomatoes + onion (3 + 8 + 6 + 2 = 19) -> 2 x 9
    public static final FoodProperties SPAGHETTI_BOLOGNESE = new FoodProperties.Builder()
            .nutrition(9).saturationModifier(0.8f).build();
    // Spaghetti & meatballs: noodles + 2 meatballs + 2 tomatoes (3 + 4 + 6 = 13) -> 2 x 7
    public static final FoodProperties SPAGHETTI_AND_MEATBALLS = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.7f).build();
    // Mutton stew: mutton + potato + carrot + onion (6 + 5 + 3 + 2 = 16) -> 2 x 8
    public static final FoodProperties MUTTON_STEW = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.7f).build();
    // Fruit salad: melon + apple + sweet berries + glow berries (2 + 4 + 2 + 2 = 10) -> 2 x 6
    public static final FoodProperties FRUIT_SALAD = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6f).build();
}
