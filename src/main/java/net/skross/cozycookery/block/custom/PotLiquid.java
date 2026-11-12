package net.skross.cozycookery.block.custom;

import net.minecraft.util.StringRepresentable;

// What's inside the cooking pot. Used as a BlockState property so the model can show it.
public enum PotLiquid implements StringRepresentable {
    EMPTY("empty"),
    WATER("water"),
    SOUP("soup");

    private final String name;

    PotLiquid(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
