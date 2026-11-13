package net.minecraft.world.item;

public final class ItemStack {
    public static final ItemStack EMPTY = new ItemStack(Items.AIR);
    private final Item item;

    public ItemStack(Item item) {
        this.item = item;
    }

    public boolean isEmpty() {
        return item == Items.AIR;
    }

    public boolean is(Item other) {
        return item == other;
    }
}
