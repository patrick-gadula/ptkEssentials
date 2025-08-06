package com.patrickg;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MoneyNoteListener implements Listener {

    private final Plugin plugin;

    public MoneyNoteListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta.getDisplayName() == null) return;

        if (ChatColor.stripColor(meta.getDisplayName()).startsWith("Money Note:")) {
            event.setCancelled(true);

            // Find amount from lore
            List<String> lore = meta.getLore();
            if (lore == null || lore.size() < 2) return;

            String amountLine = ChatColor.stripColor(lore.get(1)); // "Amount: 10000"
            if (!amountLine.startsWith("Amount: ")) return;

            int amount;
            try {
                amount = Integer.parseInt(amountLine.replace("Amount: ", ""));
            } catch (NumberFormatException e) {
                return;
            }

            // Add balance
            plugin.getBalanceManager().addBalance(player.getUniqueId(), amount);
            plugin.getBalanceManager().saveAllBalances();
            player.sendMessage(ChatColor.GREEN + "You redeemed a Money Note for $" + amount + "!");

            // Remove one note from inventory
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().removeItem(item);
            }

            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}
