package com.patrickg;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class InsuranceListener implements Listener {

    private final Plugin plugin;

    public InsuranceListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getInsuranceManager().isInsured(player)) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear(); // No drops on ground
            event.setDroppedExp(0);   // No XP loss
            plugin.getInsuranceManager().removeInsurance(player);

            player.sendMessage(ChatColor.LIGHT_PURPLE + "Your insurance saved your inventory and XP!");
        }
    }
}
