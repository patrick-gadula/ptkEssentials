package com.patrickg;

import org.bukkit.inventory.ItemStack;

public class CratePrize {

    private final ItemStack itemStack;
    private final int moneyAmount;
    private final boolean rare; // ✅ new field

    public CratePrize(ItemStack itemStack, boolean rare) {
        this.itemStack = itemStack;
        this.moneyAmount = -1;
        this.rare = rare;
    }

    public CratePrize(int moneyAmount, boolean rare) {
        this.moneyAmount = moneyAmount;
        this.itemStack = null;
        this.rare = rare;
    }

    public boolean isMoneyPrize() {
        return moneyAmount != -1;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getMoneyAmount() {
        return moneyAmount;
    }

    public boolean isRare() {
        return rare;
    }
}
