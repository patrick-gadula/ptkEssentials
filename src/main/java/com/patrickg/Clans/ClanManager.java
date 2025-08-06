package com.patrickg.Clans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ClanManager {

    private final Map<String, Clan> clans = new HashMap<>(); // clan name -> Clan object
    private final Map<UUID, String> playerClan = new HashMap<>(); // player UUID -> clan name
    private final ClanStorage clanStorage;

    public ClanManager(ClanStorage clanStorage) {
        this.clanStorage = clanStorage;
    }

    public boolean createClan(Player player, String name) {
        name = name.toLowerCase();
        if (clans.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "Clan name already exists!");
            return false;
        }
        Clan clan = new Clan(name, player.getUniqueId());
        clans.put(name, clan);
        playerClan.put(player.getUniqueId(), name);
        player.sendMessage(ChatColor.GREEN + "Clan " + name + " created!");
        return true;
    }

    public boolean joinClan(Player player, String name) {
        name = name.toLowerCase();
        if (!clans.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "Clan does not exist!");
            return false;
        }
        Clan clan = clans.get(name);
        if (playerClan.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already in a clan!");
            return false;
        }
        clan.getMembers().add(player.getUniqueId());
        playerClan.put(player.getUniqueId(), name);
        player.sendMessage(ChatColor.GREEN + "You joined the clan " + name + "!");
        return true;
    }

    public boolean leaveClan(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerClan.containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You are not in a clan!");
            return false;
        }
        String clanName = playerClan.get(uuid);
        Clan clan = clans.get(clanName);
        clan.getMembers().remove(uuid);
        playerClan.remove(uuid);

        // If no members left, delete the clan
        if (clan.getMembers().isEmpty()) {
            clans.remove(clanName);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Clan " + clanName + " has been disbanded!");
            clanStorage.saveClans(clans); // 🔥 Immediately update YAML
        } else {
            clanStorage.saveClans(clans); // 🔥 Even if not disbanded, save the new member list
        }
    
        player.sendMessage(ChatColor.GREEN + "You have left the clan " + clanName + ".");
        return true;
    }

    public boolean sameClan(Player p1, Player p2) {
        return playerClan.containsKey(p1.getUniqueId()) &&
                playerClan.containsKey(p2.getUniqueId()) &&
                playerClan.get(p1.getUniqueId()).equals(playerClan.get(p2.getUniqueId()));
    }

    public Clan getClanByPlayer(Player player) {
        String name = playerClan.get(player.getUniqueId());
        if (name == null) return null;
        return clans.get(name);
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }

    public void registerLoadedClan(Clan clan) {
        clans.put(clan.getName().toLowerCase(), clan);
        for (UUID member : clan.getMembers()) {
            playerClan.put(member, clan.getName().toLowerCase());
        }
    }

    public Map<String, Clan> getClans() {
        return clans;
    }

}