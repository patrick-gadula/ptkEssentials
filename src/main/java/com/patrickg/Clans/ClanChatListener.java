package com.patrickg.Clans;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ClanChatListener implements Listener {

    private final ClanManager clanManager;
    private final ClanPrefixManager prefixManager;

    public ClanChatListener(ClanManager clanManager, ClanPrefixManager prefixManager) {
        this.clanManager = clanManager;
        this.prefixManager = prefixManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Clan clan = clanManager.getClanByPlayer(player);

        String prefix = "";
        if (clan != null) {
            prefix = ChatColor.GRAY + "[ " + ChatColor.AQUA + clan.getName().toUpperCase() + ChatColor.GRAY + " ] ";
        }

        event.setFormat(prefix + ChatColor.RESET + player.getDisplayName() + ChatColor.WHITE + ": " + ChatColor.RESET + "%2$s");

    }
}
