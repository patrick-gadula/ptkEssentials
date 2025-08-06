package com.patrickg.Clans;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

public class ClanPvpListener implements Listener {

    private final ClanManager clanManager;

    public ClanPvpListener(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        if (clanManager.sameClan(damaged, damager)) {
            event.setCancelled(true);
            damager.sendMessage("§cYou can't hurt members of your own clan!");
        }
    }
}
