package net.minecraft.client.player;

import net.minecraft.world.item.ItemStack;

public final class LocalPlayer {
    public ItemStack useItem = ItemStack.EMPTY;

    public boolean isUsingItem() {
        return !useItem.isEmpty();
    }

    public ItemStack getUseItem() {
        return useItem;
    }
}
